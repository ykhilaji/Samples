package com.nryanov.calculator

import cats.Show
import cats.effect.Sync
import pureconfig._
import pureconfig.generic.ProductHint
import pureconfig.generic.auto._

case class DataSourceConfiguration(
                       driver: String,
                       url: String,
                       schema: String,
                       username: String,
                       password: String
                     )

case class Configuration(
                          database: DataSourceConfiguration
                        )

object Configuration {
  implicit val configurationShow: Show[Configuration] = Show.show[Configuration](_.toString)
  implicit def hint[T]: ProductHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

  def load[F[_] : Sync]: F[Configuration] = Sync[F].delay(ConfigSource.default.loadOrThrow[Configuration])
}
