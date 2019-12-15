package com.nryanov.calculator.mq

import java.nio.charset.StandardCharsets
import java.util.UUID

import cats.data.Kleisli
import cats.implicits._
import fs2.Stream
import cats.effect.{ConcurrentEffect, ContextShift, Sync}
import com.nryanov.calculator.config.Configuration
import dev.profunktor.fs2rabbit.effects.MessageEncoder
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model.{AMQPChannel, AmqpEnvelope, AmqpMessage, AmqpProperties, ExchangeName, RoutingKey}
import org.pure4s.logger4s.LazyLogging
import org.pure4s.logger4s.cats.Logger

class RabbitMqRpcClient[F[_] : Sync](configuration: Configuration)(implicit rabbitClient: RabbitClient[F], channel: AMQPChannel) extends LazyLogging {
  private val rpcRoutingKey = RoutingKey(configuration.rabbitmq.queue.rpcQueue)
  private val rpcExchange = ExchangeName(configuration.rabbitmq.exchange.rpcExchange)

  implicit val encoder: MessageEncoder[F, AmqpMessage[String]] =
    Kleisli[F, AmqpMessage[String], AmqpMessage[Array[Byte]]](s => s.copy(payload = s.payload.getBytes(StandardCharsets.UTF_8)).pure[F])

  def call(body: String): Stream[F, AmqpEnvelope[String]] = {
    val correlationId = UUID.randomUUID().toString

    for {
      replyQueue <- Stream.eval(rabbitClient.declareQueue)
      publisher <- Stream.eval(rabbitClient.createPublisher[AmqpMessage[String]](rpcExchange, rpcRoutingKey))
      _ <- Stream.eval(Logger[F].info(s"[Client] Message $body. ReplyTo queue $replyQueue. Correlation $correlationId"))
      message = AmqpMessage(body, AmqpProperties(replyTo = Some(replyQueue.value), correlationId = Some(correlationId)))
      _ <- Stream.eval(Logger[F].info(s"[Client] Publish message"))
      _ <- Stream.eval(publisher(message))
      _ <- Stream.eval(Logger[F].info(s"[Client] Message published"))
      _ <- Stream.eval(Logger[F].info(s"[Client] Read response"))
      consumer <- Stream.eval(rabbitClient.createAutoAckConsumer(replyQueue))
      response <- consumer.filter(_.properties.correlationId.contains(correlationId)).take(1)
      _ <- Stream.eval(Logger[F].info(s"[Client] Request $body. Received response [${response.payload}]"))
    } yield response
  }
}

object RabbitMqRpcClient {
  def apply[F[_] : Sync](configuration: Configuration)(implicit rabbitClient: RabbitClient[F], channel: AMQPChannel): RabbitMqRpcClient[F] =
    new RabbitMqRpcClient(configuration)
}