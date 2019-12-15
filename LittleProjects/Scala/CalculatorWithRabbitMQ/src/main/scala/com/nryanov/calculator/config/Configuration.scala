package com.nryanov.calculator.config

import cats.Show
import cats.effect.{Resource, Sync}
import cats.implicits._
import org.pure4s.logger4s.LazyLogging
import org.pure4s.logger4s.cats.Logger
import pureconfig._
import pureconfig.generic.ProductHint
import pureconfig.generic.auto._

final case class DataBaseConfig(
                                 driver: String,
                                 url: String,
                                 schema: String,
                                 username: String,
                                 password: String
                               )

// all exchanges - direct
final case class ExchangeConfig(
                                 rpcExchange: String
                               )

final case class QueueConfig(
                              rpcQueue: String
                            )

final case class RabbitMQConfig(
                                 virtualHost: String,
                                 host: String,
                                 port: Int,
                                 username: Option[String] = Some("guest"),
                                 password: Option[String] = Some("guest"),
                                 exchange: ExchangeConfig,
                                 queue: QueueConfig
                               )

final case class Configuration(
                                database: DataBaseConfig,
                                rabbitmq: RabbitMQConfig
                              )

object Configuration extends LazyLogging {
  implicit val configurationShow: Show[Configuration] = Show.show[Configuration](_.toString)

  implicit def hint[T]: ProductHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

  def load[F[_] : Sync]: Resource[F, Configuration] = Resource.liftF(load0)

  private def load0[F[_]](implicit F: Sync[F]): F[Configuration] = for {
    _ <- Logger[F].info("Load configuration")
    cfg <- F.delay(ConfigSource.default.loadOrThrow[Configuration])
    _ <- Logger[F].info(s"Configuration: $cfg")
  } yield cfg
}
