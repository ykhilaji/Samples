package com.nryanov.calculator.datasource

import cats.effect.{Async, Blocker, ContextShift, Resource, Sync}
import com.nryanov.calculator.config.Configuration
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import org.pure4s.logger4s.LazyLogging
import org.pure4s.logger4s.cats.Logger

object DataSource extends LazyLogging {
  def createConfig[F[_] : Sync](configuration: Configuration): Resource[F, HikariConfig] = Resource.pure(createConfig0(configuration))

  def create[F[_] : Async : ContextShift](configuration: HikariConfig): Resource[F, Transactor[F]] = for {
    _ <- Resource.liftF(Logger[F].info("Create datasource"))
    connectionExecutionPool <- ExecutionContexts.fixedThreadPool[F](Runtime.getRuntime.availableProcessors() * 2)
    transactionExecutionPool <- ExecutionContexts.cachedThreadPool[F]
    xa <- HikariTransactor.fromHikariConfig[F](
      configuration,
      connectionExecutionPool,
      Blocker.liftExecutionContext(transactionExecutionPool)
    )
    _ <- Resource.liftF(Logger[F].info("Datasource created"))
  } yield xa

  private def createConfig0(configuration: Configuration): HikariConfig = {
    val cfg = new HikariConfig()
    cfg.setDriverClassName(configuration.database.driver)
    cfg.setJdbcUrl(configuration.database.url)
    cfg.setUsername(configuration.database.username)
    cfg.setPassword(configuration.database.password)
    cfg.setSchema(configuration.database.schema)
    cfg.setMinimumIdle(1)

    cfg
  }
}
