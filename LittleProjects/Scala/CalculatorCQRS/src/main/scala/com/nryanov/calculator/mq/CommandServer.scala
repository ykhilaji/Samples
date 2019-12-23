package com.nryanov.calculator.mq

import fs2._
import cats.effect.{Resource, Sync}
import cats.implicits._
import cats.~>
import com.nryanov.calculator.configuration.Configuration
import com.nryanov.calculator.logic.ExpressionExecutor
import com.nryanov.calculator.repository.Repository
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model
import dev.profunktor.fs2rabbit.model.{AMQPConnection, QueueName}
import org.pure4s.logger4s.LazyLogging
import org.pure4s.logger4s.cats.Logger

class CommandServer[F[_]](
                       consumer: Stream[F, model.AmqpEnvelope[String]],
                       repository: Repository[F],
                       expressionExecutor: ExpressionExecutor[F],
                       db: doobie.ConnectionIO ~> F
                     )(implicit F: Sync[F]) extends LazyLogging {

  def serve(): Stream[F, Unit] = consumer.evalMap { envelope =>
    for {
      uuid <- envelope.properties.correlationId.toRight(new IllegalArgumentException("UUID is empty")).liftTo[F]
      // probably, this step should be in CommandClient
      _ <- db(repository.insertExpression(uuid, envelope.payload))
      _ <- Logger[F].info(s"Get new expression: ${envelope.payload} with uuid: $uuid")
      _ <- expressionExecutor.execute(envelope.payload)
        .flatMap(result => Logger[F].info(s"Result: $result") *> db(repository.insertExpressionResult(uuid, result)))
        .handleErrorWith(error => Logger[F].info(s"Error: ${error.getLocalizedMessage}") *> db(repository.insertExpressionError(uuid, error)))
    } yield ()
  }
}

object CommandServer extends LazyLogging {
  def apply[F[_]](
                   cfg: Configuration, rabbitClient: RabbitClient[F],
                   connection: AMQPConnection,
                   repository: Repository[F],
                   expressionExecutor: ExpressionExecutor[F],
                   db: doobie.ConnectionIO ~> F
                 )(implicit F: Sync[F]): Resource[F, CommandServer[F]] =
    rabbitClient.createChannel(connection).evalMap { implicit channel =>
      for {
        _ <- Logger[F].info("Creating server")
        _ <- Logger[F].info("Creating consumer")
        consumer <- rabbitClient.createAutoAckConsumer(QueueName(cfg.rabbitmq.queue.rpcQueue))
        _ <- Logger[F].info("Successfully created consumer")
        _ <- Logger[F].info("Successfully created server")
      } yield new CommandServer[F](consumer, repository, expressionExecutor, db)
    }

}