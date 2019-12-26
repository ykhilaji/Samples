package com.nryanov.calculator.mq

import brave.Span.Kind
import brave.propagation.TraceContext
import brave.{Span, Tracing}
import fs2._
import cats.effect.{Bracket, Resource, Sync}
import cats.implicits._
import cats.~>
import com.nryanov.calculator.configuration.Configuration
import com.nryanov.calculator.logic.ExpressionExecutor
import com.nryanov.calculator.repository.Repository
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model
import dev.profunktor.fs2rabbit.model.{AMQPConnection, AmqpEnvelope, AmqpFieldValue, QueueName}
import org.pure4s.logger4s.LazyLogging
import org.pure4s.logger4s.cats.Logger


class CommandServer[F[_]](
                           consumer: Stream[F, model.AmqpEnvelope[String]],
                           repository: Repository[F],
                           expressionExecutor: ExpressionExecutor[F],
                           db: doobie.ConnectionIO ~> F,
                           tracing: Tracing
                         )(implicit F: Sync[F]) extends LazyLogging {

  private val extractor: TraceContext.Extractor[Map[String, AmqpFieldValue]] = tracing.propagation()
    .extractor((carrier: Map[String, AmqpFieldValue], key: String) => {
      carrier.get(key).map(_.toValueWriterCompatibleJava.toString).getOrElse("")
    })

  def serve(): Stream[F, Unit] = consumer.evalMap { envelope =>
    trace(envelope)(process(envelope))
  }

  private def trace(envelope: AmqpEnvelope[String])(fa: F[Unit]): F[Unit] = Bracket[F, Throwable].bracket(extractContext(envelope))({ span =>
    fa.onError {
      case error: Throwable => F.delay(span.error(error)).void
    }
  })(span => F.delay(span.finish()))

  private def extractContext(envelope: AmqpEnvelope[String]): F[Span] =
    F.delay(extractor.extract(envelope.properties.headers))
    .map(extracted => {
      if (extracted.context() == null) {
        tracing.tracer().nextSpan(extracted)
      } else {
        tracing.tracer().joinSpan(extracted.context())
      }
    })
    .map(span => span.name("process").kind(Kind.SERVER).start())

  private def process(envelope: AmqpEnvelope[String]): F[Unit] = for {
    uuid <- envelope.properties.correlationId.toRight(new IllegalArgumentException("UUID is empty")).liftTo[F]
    // probably, this step should be in CommandClient
    _ <- db(repository.insertExpression(uuid, envelope.payload))
    _ <- Logger[F].info(s"Get new expression: ${envelope.payload} with uuid: $uuid")
    _ <- expressionExecutor.execute(envelope.payload)
      .flatMap(result => Logger[F].info(s"Result: $result") *> db(repository.insertExpressionResult(uuid, result)))
      .handleErrorWith(error => Logger[F].info(s"Error: ${error.getLocalizedMessage}") *> db(repository.insertExpressionError(uuid, error)))
  } yield ()
}

object CommandServer extends LazyLogging {
  def apply[F[_]](
                   cfg: Configuration, rabbitClient: RabbitClient[F],
                   connection: AMQPConnection,
                   repository: Repository[F],
                   expressionExecutor: ExpressionExecutor[F],
                   db: doobie.ConnectionIO ~> F,
                   tracing: Tracing
                 )(implicit F: Sync[F]): Resource[F, CommandServer[F]] =
    rabbitClient.createChannel(connection).evalMap { implicit channel =>
      for {
        _ <- Logger[F].info("Creating server")
        _ <- Logger[F].info("Creating consumer")
        consumer <- rabbitClient.createAutoAckConsumer(QueueName(cfg.rabbitmq.queue.rpcQueue))
        _ <- Logger[F].info("Successfully created consumer")
        _ <- Logger[F].info("Successfully created server")
      } yield new CommandServer[F](consumer, repository, expressionExecutor, db, tracing)
    }

}