package com.nryanov.calculator

import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import cats.effect.{Async, Blocker, ContextShift, Resource}

object DataSource {

  def transactor[F[_] : Async : ContextShift](configuration: Configuration): Resource[F, Transactor[F]] = {
    val cfg = new HikariConfig()
    cfg.setDriverClassName(configuration.database.driver)
    cfg.setJdbcUrl(configuration.database.url)
    cfg.setUsername(configuration.database.username)
    cfg.setPassword(configuration.database.password)
    cfg.setSchema(configuration.database.schema)
    cfg.setMinimumIdle(1)

    for {
      connectionExecutionPool <- ExecutionContexts.fixedThreadPool[F](Runtime.getRuntime.availableProcessors())
      transactionExecutionPool <- ExecutionContexts.cachedThreadPool[F]
      xa <- HikariTransactor.fromHikariConfig[F](
        cfg,
        connectionExecutionPool,
        Blocker.liftExecutionContext(transactionExecutionPool)
      )
    } yield xa
  }
}
