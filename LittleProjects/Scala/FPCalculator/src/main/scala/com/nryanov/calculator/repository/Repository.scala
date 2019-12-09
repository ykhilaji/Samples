package com.nryanov.calculator.repository

import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.pure4s.logger4s.LazyLogging
import org.pure4s.logger4s.cats.Logger
import cats.effect.{Async, Resource, Sync}
import cats.implicits._

class Repository[F[_]: Sync](db: Resource[F, Transactor[F]]) extends LazyLogging {
  def storeExpression(expression: String): F[Long] = for {
    _ <- Logger[F].info(s"Store expression: $expression")
    id <- storeExpressionInternal(expression)
    _ <- Logger[F].info(s"Expression id: $id")
  } yield id

  def storeResult(expressionId: Long, result: Double): F[Unit] = for {
    _ <- Logger[F].info(s"Store result[$expressionId]: $result")
    _ <- storeResultInternal(expressionId, result)
  } yield ()

  def storeResultError(expressionId: Long, error: Throwable): F[Unit] = for {
    _ <- Logger[F].info(s"Store error[$expressionId]: ${error.getLocalizedMessage}")
    _ <- storeResultErrorInternal(expressionId, error)
  } yield ()

  private def storeExpressionInternal(expression: String): F[Long] = db.use[Long] { xa =>
    sql"insert into expression (expression) values ($expression)"
      .update
      .withUniqueGeneratedKeys[Long]("expression_id")
      .transact(xa)
  }

  private def storeResultInternal(expressionId: Long, result: Double): F[Unit] = db.use[Unit] { xa =>
    sql"insert into result (expression_id, result) values ($expressionId, $result)"
      .update
      .run
      .map(_ => ())
      .transact(xa)
  }

  private def storeResultErrorInternal(expressionId: Long, error: Throwable): F[Unit] = db.use[Unit] { xa =>
    sql"insert into result (expression_id, error) values ($expressionId, ${error.getLocalizedMessage})"
      .update
      .run
      .map(_ => ())
      .transact(xa)
  }
}

object Repository {
  def apply[F[_]: Sync](db: Resource[F, Transactor[F]]): Repository[F] = new Repository(db)
}
