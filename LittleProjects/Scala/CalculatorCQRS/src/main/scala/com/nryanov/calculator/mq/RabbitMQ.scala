package com.nryanov.calculator.mq

import cats.data.NonEmptyList._
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, Resource, Sync}
import cats.syntax.apply._
import cats.syntax.applicativeError._
import com.nryanov.calculator.configuration.Configuration
import dev.profunktor.fs2rabbit.config.declaration.DeclarationQueueConfig
import dev.profunktor.fs2rabbit.config.{Fs2RabbitConfig, Fs2RabbitNodeConfig}
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model.{ExchangeName, ExchangeType, QueueName, RoutingKey}
import org.pure4s.logger4s.LazyLogging
import org.pure4s.logger4s.cats.Logger

object RabbitMQ extends LazyLogging {
  def config[F[_] : Sync](configuration: Configuration): Resource[F, Fs2RabbitConfig] = Resource.pure(config0(configuration))

  def client[F[_] : ConcurrentEffect : ContextShift](rabbitCfg: Fs2RabbitConfig, blocker: Blocker): Resource[F, RabbitClient[F]] = for {
    _ <- Resource.liftF(Logger[F].info("Creating rabbitmq client"))
    client <- Resource.liftF(RabbitClient[F](rabbitCfg, blocker))
    _ <- Resource.liftF(Logger[F].info("Successfully created rabbitmq client"))
  } yield client

  def init[F[_] : ContextShift](configuration: Configuration, rabbitClient: RabbitClient[F])(implicit F: ConcurrentEffect[F]): F[Unit] =
    rabbitClient.createConnectionChannel.use { implicit channel =>
      Logger[F].info("Declare rpc exchange") *>
        rabbitClient.declareExchange(ExchangeName(configuration.rabbitmq.exchange.rpcExchange), ExchangeType.Direct) *>
        Logger[F].info("Declare rpc queue") *>
        rabbitClient.declareQueue(DeclarationQueueConfig.default(QueueName(configuration.rabbitmq.queue.rpcQueue))) *>
        Logger[F].info("Bind rpc queue and rpc exchange") *>
        rabbitClient.bindQueue(QueueName(configuration.rabbitmq.queue.rpcQueue), ExchangeName(configuration.rabbitmq.exchange.rpcExchange), RoutingKey(configuration.rabbitmq.queue.rpcQueue))
    }.handleErrorWith(e => F.delay(Logger[F].error(e.getLocalizedMessage)) *> e.raiseError)

  private def config0(configuration: Configuration) = Fs2RabbitConfig(
    virtualHost = configuration.rabbitmq.virtualHost,
    nodes = one(
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
  )
}
