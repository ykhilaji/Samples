package com.nryanov.calculator.mq

import java.util.UUID

import brave.Span.Kind
import brave.propagation.TraceContext
import brave.{Tracer, Tracing}
import cats.effect.{Bracket, Resource, Sync}
import cats.implicits._
import com.nryanov.calculator.configuration.Configuration
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model.{AMQPConnection, AmqpFieldValue, AmqpMessage, AmqpProperties, ExchangeName, RoutingKey}
import org.pure4s.logger4s.LazyLogging
import org.pure4s.logger4s.cats.Logger

import scala.collection.mutable


class CommandClient[F[_]](
                           publisher: AmqpMessage[String] => F[Unit],
                           tracing: Tracing,
                         )(implicit F: Sync[F]) extends LazyLogging {
  private val injector: TraceContext.Injector[mutable.Map[String, AmqpFieldValue]] = tracing
    .propagation()
    .injector((carrier: mutable.Map[String, AmqpFieldValue], key: String, value: String) => carrier += ((key, AmqpFieldValue.StringVal(value))))

  def call(expression: String): F[String] = Bracket[F, Throwable].bracket(F.delay(tracing.tracer().nextSpan().name("call").kind(Kind.CLIENT)))({ span =>
    for {
      headers <- F.pure(mutable.Map[String, AmqpFieldValue]())
      _ <- F.delay(injector.inject(span.context(), headers))
      result <- call0(expression, headers).onError {
        case error: Throwable => F.delay(span.error(error)).void
      }
    } yield result
  })(span => F.delay(span.start().finish()))

  private def call0(expression: String, headers: mutable.Map[String, AmqpFieldValue]): F[String] = for {
    _ <- Logger[F].info(s"Create new expression command: $expression")
    uuid <- F.delay(UUID.randomUUID().toString)
    _ <- Logger[F].info(s"UUID: $uuid")
    message = AmqpMessage[String](expression, AmqpProperties(correlationId = uuid.some, headers = headers.toMap))
    _ <- publisher(message)
  } yield uuid
}

object CommandClient extends LazyLogging {

  def apply[F[_]](cfg: Configuration, rabbitClient: RabbitClient[F], connection: AMQPConnection, tracing: Tracing)(implicit F: Sync[F]): Resource[F, CommandClient[F]] =
    rabbitClient.createChannel(connection).evalMap { implicit channel =>
      for {
        _ <- Logger[F].info("Creating client")
        _ <- Logger[F].info("Creating publisher")
        publisher <- rabbitClient.createPublisher[AmqpMessage[String]](ExchangeName(cfg.rabbitmq.exchange.rpcExchange), RoutingKey(cfg.rabbitmq.queue.rpcQueue))
        _ <- Logger[F].info("Successfully created publisher")
        _ <- Logger[F].info("Successfully created client")
      } yield new CommandClient(publisher, tracing)
    }
}
