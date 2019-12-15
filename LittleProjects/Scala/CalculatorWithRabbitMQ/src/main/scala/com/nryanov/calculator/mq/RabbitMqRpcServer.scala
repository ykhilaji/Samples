package com.nryanov.calculator.mq

import java.nio.charset.StandardCharsets

import cats.data.Kleisli
import cats.effect.Sync
import com.nryanov.calculator.config.Configuration
import fs2.Stream
import cats.implicits._
import com.nryanov.calculator.logic.ExpressionExecutor
import com.nryanov.calculator.repository.ExpressionRepository
import dev.profunktor.fs2rabbit.effects.MessageEncoder
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model.{AMQPChannel, AmqpEnvelope, AmqpMessage, AmqpProperties, ExchangeName, QueueName, RoutingKey}
import org.pure4s.logger4s.LazyLogging
import org.pure4s.logger4s.cats.Logger

class RabbitMqRpcServer[F[_] : Sync](configuration: Configuration, expressionExecutor: ExpressionExecutor, expressionRepository: ExpressionRepository[F])(implicit rabbitClient: RabbitClient[F], channel: AMQPChannel)
  extends LazyLogging {
  private val rpcQueue = QueueName(configuration.rabbitmq.queue.rpcQueue)
  private val emptyExchange = ExchangeName("")

  private implicit val encoder: MessageEncoder[F, AmqpMessage[String]] =
    Kleisli[F, AmqpMessage[String], AmqpMessage[Array[Byte]]](s => s.copy(payload = s.payload.getBytes(StandardCharsets.UTF_8)).pure[F])

  def serve(): Stream[F, AmqpEnvelope[String]] =
    Stream.eval(rabbitClient.createAutoAckConsumer[String](rpcQueue)).flatMap(_.evalTap(handler))

  private def handler(envelope: AmqpEnvelope[String]): F[Unit] = {
    val correlationId = envelope.properties.correlationId
    val replyTo = envelope.properties.replyTo.toRight(new IllegalArgumentException("ReplyTo parameter is missing"))

    for {
      rk <- replyTo.liftTo[F]
      _ <- Logger[F].info(s"[Server] Received message [${envelope.payload}]. ReplyTo $rk. CorrelationId $correlationId")
      expressionId <- expressionRepository.storeExpression(envelope.payload)
      result <- expressionExecutor.execute[F](envelope.payload)
        .flatMap(result => expressionRepository.storeResult(expressionId, result) *> Sync[F].pure(result.toString))
        .handleErrorWith(e => expressionRepository.storeResultError(expressionId, e) *> Sync[F].pure(e.getLocalizedMessage))
      publisher <- rabbitClient.createPublisher[AmqpMessage[String]](emptyExchange, RoutingKey(rk))
      response = AmqpMessage(result, AmqpProperties(correlationId = correlationId))
      _ <- publisher(response)
    } yield ()
  }
}

object RabbitMqRpcServer {
  def apply[F[_] : Sync](configuration: Configuration, expressionExecutor: ExpressionExecutor, expressionRepository: ExpressionRepository[F])(implicit rabbitClient: RabbitClient[F], channel: AMQPChannel): RabbitMqRpcServer[F] =
    new RabbitMqRpcServer(configuration, expressionExecutor, expressionRepository)
}