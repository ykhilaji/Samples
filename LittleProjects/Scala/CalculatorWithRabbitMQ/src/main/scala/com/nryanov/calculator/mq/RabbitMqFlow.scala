package com.nryanov.calculator.mq

import cats.syntax.apply._
import cats.syntax.applicativeError._
import cats.data.NonEmptyList
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, Resource, Sync}
import com.nryanov.calculator.config.Configuration
import dev.profunktor.fs2rabbit.config.declaration.DeclarationQueueConfig
import dev.profunktor.fs2rabbit.config.{Fs2RabbitConfig, Fs2RabbitNodeConfig}
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model.{AMQPChannel, ExchangeName, ExchangeType, QueueName, RoutingKey}
import org.pure4s.logger4s.LazyLogging
import org.pure4s.logger4s.cats.Logger

object RabbitMqFlow extends LazyLogging {
  def rabbitConfig[F[_] : Sync](configuration: Configuration): Resource[F, Fs2RabbitConfig] = for {
    _ <- Resource.liftF(Logger[F].info("Create rabbitmq config"))
    cfg <- rabbitConfig0(configuration)
    _ <- Resource.liftF(Logger[F].info(s"RabbitMQ config: $cfg"))
  } yield cfg

  def create[F[_] : ConcurrentEffect : ContextShift](rabbitCfg: Fs2RabbitConfig, blocker: Blocker): Resource[F, RabbitClient[F]] = for {
    _ <- Resource.liftF(Logger[F].info("Creating RabbitMQ client"))
    client <- Resource.liftF(RabbitClient[F](rabbitCfg, blocker))
    _ <- Resource.liftF(Logger[F].info("Successfully created RabbitMQ client"))
  } yield client

  def init[F[_]](configuration: Configuration, rabbitClient: RabbitClient[F])(implicit F: ConcurrentEffect[F]): F[Unit] =
    rabbitClient.createConnectionChannel.use { implicit channel =>
      F.delay(Logger[F].info("Create exchanges")) *>
        createExchanges(configuration, rabbitClient) *>
        F.delay(Logger[F].info("Create queues")) *>
        createQueues(configuration, rabbitClient) *>
        F.delay(Logger[F].info("Bind exchanges and queues")) *>
        bindQueuesToExchanges(configuration, rabbitClient)
    }.handleError(e => println(e.getLocalizedMessage))

  private def rabbitConfig0[F[_] : Sync](configuration: Configuration): Resource[F, Fs2RabbitConfig] =
    Resource.pure(Fs2RabbitConfig(
      virtualHost = configuration.rabbitmq.virtualHost,
      nodes = NonEmptyList.one(
        Fs2RabbitNodeConfig(
          host = configuration.rabbitmq.host,
          port = configuration.rabbitmq.port
        )
      ),
      username = configuration.rabbitmq.username,
      password = configuration.rabbitmq.password,
      ssl = false,
      connectionTimeout = 100,
      requeueOnNack = false,
      internalQueueSize = Some(500),
      automaticRecovery = true
    ))

  private def createExchanges[F[_] : ConcurrentEffect](configuration: Configuration, rabbitClient: RabbitClient[F])(implicit channel: AMQPChannel): F[Unit] =
    rabbitClient.declareExchange(ExchangeName(configuration.rabbitmq.exchange.rpcExchange), ExchangeType.Direct)

  private def createQueues[F[_] : ConcurrentEffect](configuration: Configuration, rabbitClient: RabbitClient[F])(implicit channel: AMQPChannel): F[Unit] =
    rabbitClient.declareQueue(DeclarationQueueConfig.default(QueueName(configuration.rabbitmq.queue.rpcQueue)))

  private def bindQueuesToExchanges[F[_] : ConcurrentEffect](configuration: Configuration, rabbitClient: RabbitClient[F])(implicit channel: AMQPChannel): F[Unit] =
    rabbitClient.bindQueue(QueueName(configuration.rabbitmq.queue.rpcQueue), ExchangeName(configuration.rabbitmq.exchange.rpcExchange), RoutingKey(configuration.rabbitmq.queue.rpcQueue))
}
