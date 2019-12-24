package com.nryanov.calculator.db

import java.util

import cats.effect.Sync
import cats.syntax.functor._
import com.nryanov.calculator.configuration.Configuration
import org.flywaydb.core.Flyway
import org.flywaydb.core.internal.configuration.ConfigUtils

import scala.collection.JavaConverters._


object FlywayMigrationTool {
  def migrate[F[_] : Sync](cfg: Configuration): F[Unit] =
    Sync[F].delay(flywayInstance(cfg).migrate()).void

  private def flywayInstance(cfg: Configuration): Flyway = Flyway
    .configure()
    .configuration(flywayConfiguration())
    .dataSource(cfg.database.url, cfg.database.username, cfg.database.password)
    .load()

  private def flywayConfiguration(): util.Map[String, String] = Map(
    ConfigUtils.LOCATIONS -> "db/migration",
  ).asJava
}
