package com.nryanov.calculator.repository

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import doobie._
import doobie.implicits._
import cats.effect.Sync
import com.nryanov.calculator.model.{Expression, Result}

class Repository[F[_] : Sync] {
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")

  implicit val localDateTimePut: Meta[LocalDateTime] = Meta[String].timap(LocalDateTime.parse(_, formatter))(_.toString)

  def insertExpression(uuid: String, expression: String): doobie.ConnectionIO[Int] =
    sql"insert into expression(expression_id, expression) values ($uuid, $expression)"
      .update
      .run

  def insertExpressionResult(expressionId: String, result: Double): doobie.ConnectionIO[Int] =
    sql"insert into result(expression_id, result) values ($expressionId, $result)"
      .update
      .run

  def insertExpressionError(expressionId: String, reason: Throwable): doobie.ConnectionIO[Int] =
    sql"insert into result(expression_id, error) values ($expressionId, {reason.getLocalizedMessage})"
      .update
      .run

  def selectExpression(expressionId: String): doobie.ConnectionIO[Option[Expression]] =
    sql"select expression_id, expression, creation_time from expression where expression_id = $expressionId"
      .query[(String, String, LocalDateTime)]
      .map(v => Expression(v._1, v._2, v._3))
      .option

  def selectExpressionResult(expressionId: String): doobie.ConnectionIO[Option[Result]] =
    sql"select expression_id, result, error, creation_time from result where expression_id = $expressionId"
      .query[(String, Option[Double], Option[String], LocalDateTime)]
      .map(v => Result(v._1, v._2, v._3, v._4))
      .option

  def selectExpressionsByTime(from: LocalDateTime, to: LocalDateTime): doobie.ConnectionIO[List[Expression]] =
    sql"select expression_id, expression, creation_time from expression where creation_time >= $from and creation_time < $to"
      .query[(String, String, LocalDateTime)]
      .map(v => Expression(v._1, v._2, v._3))
      .to[List]

  def selectExpressionResultsByTime(from: LocalDateTime, to: LocalDateTime): doobie.ConnectionIO[List[Result]] =
    sql"select expression_id, result, error, creation_time from result where creation_time >= $from and creation_time < $to"
      .query[(String, Option[Double], Option[String], LocalDateTime)]
      .map(v => Result(v._1, v._2, v._3, v._4))
      .to[List]
}

object Repository {

  def apply[F[_] : Sync](): Repository[F] = new Repository[F]()
}