package com.github.gr1f0n6x.scalatra.doobie.crud.configuration

import doobie._
import doobie.implicits._
import cats.effect._
import scala.concurrent.ExecutionContext

object DataSource {
  implicit val cs = IO.contextShift(ExecutionContext.global)

  lazy val xa = {
    val xa = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/postgres",
      "postgres",
      ""
    )

    sql"create table if not exists entity (id serial primary key, value text)"
      .update
      .run
      .transact(xa)
      .unsafeRunSync()

    xa
  }
}
