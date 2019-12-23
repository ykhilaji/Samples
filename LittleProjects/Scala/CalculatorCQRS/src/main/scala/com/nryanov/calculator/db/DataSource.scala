package com.nryanov.calculator.db

import cats.arrow.FunctionK
import cats.effect.{Async, Blocker, ContextShift, Resource}
import cats.~>
import com.nryanov.calculator.configuration.Configuration
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import doobie.implicits._

object DataSource {
  def resource[F[_] : Async : ContextShift](configuration: Configuration): Resource[F, doobie.ConnectionIO ~> F] =
    transactor[F](configuration).map { tx =>
      def transact[A](tx: Transactor[F])(sql: doobie.ConnectionIO[A]): F[A] =
        sql.transact(tx)

      new FunctionK[doobie.ConnectionIO, F] {
        def apply[A](l: doobie.ConnectionIO[A]): F[A] = transact(tx)(l)
      }
    }

  private def transactor[F[_] : Async : ContextShift](configuration: Configuration): Resource[F, Transactor[F]] = for {
    ce <- ExecutionContexts.fixedThreadPool[F](8) // our connect EC
    be <- Blocker[F] // cached thread pool
    cfg <- Resource.liftF(Async[F].delay(hikariCfg(configuration)))
    xa <- HikariTransactor.fromHikariConfig[F](
      cfg,
      ce, // await connection here
      be // execute JDBC operations here
    )
  } yield xa

  private def hikariCfg(configuration: Configuration) = {
    val cfg = new HikariConfig()

    cfg.setDriverClassName(configuration.database.driver)
    cfg.setJdbcUrl(configuration.database.url)
    cfg.setUsername(configuration.database.username)
    cfg.setPassword(configuration.database.password)

    cfg
  }
}
