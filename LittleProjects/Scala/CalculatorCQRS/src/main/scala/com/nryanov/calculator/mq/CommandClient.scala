package com.nryanov.calculator.mq

import java.util.UUID

import cats.effect.{Resource, Sync}
import cats.implicits._
import com.nryanov.calculator.configuration.Configuration
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model.{AMQPConnection, AmqpMessage, AmqpProperties, ExchangeName, RoutingKey}
import org.pure4s.logger4s.LazyLogging
import org.pure4s.logger4s.cats.Logger

class CommandClient[F[_]](publisher: AmqpMessage[String] => F[Unit])(implicit F: Sync[F]) extends LazyLogging {
  def call(expression: String): F[String] = for {
    _ <- Logger[F].info(s"Create new expression command: $expression")
    uuid <- F.delay(UUID.randomUUID().toString)
    _ <- Logger[F].info(s"UUID: $uuid")
    message = AmqpMessage[String](expression, AmqpProperties(correlationId = uuid.some))
    _ <- publisher(message)
  } yield uuid
}

object CommandClient extends LazyLogging {

  def apply[F[_]](cfg: Configuration, rabbitClient: RabbitClient[F], connection: AMQPConnection)(implicit F: Sync[F]): Resource[F, CommandClient[F]] =
    rabbitClient.createChannel(connection).evalMap { implicit channel =>
      for {
        _ <- Logger[F].info("Creating client")
        _ <- Logger[F].info("Creating publisher")
        publisher <- rabbitClient.createPublisher[AmqpMessage[String]](ExchangeName(cfg.rabbitmq.exchange.rpcExchange), RoutingKey(cfg.rabbitmq.queue.rpcQueue))
        _ <- Logger[F].info("Successfully created publisher")
        _ <- Logger[F].info("Successfully created client")
      } yield new CommandClient(publisher)
    }
}
