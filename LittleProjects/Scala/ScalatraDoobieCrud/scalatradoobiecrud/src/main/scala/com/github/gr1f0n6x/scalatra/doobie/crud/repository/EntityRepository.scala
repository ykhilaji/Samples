package com.github.gr1f0n6x.scalatra.doobie.crud.repository

import cats.effect.IO
import com.github.gr1f0n6x.scalatra.doobie.crud.model.Entity
import doobie.implicits._
import com.github.gr1f0n6x.scalatra.doobie.crud.configuration.DataSource.xa

class EntityRepository extends Repository[Entity, Long] {
  override def findOne(id: Long): IO[Entity] =
    sql"select id, value from entity where id = $id"
      .query[Entity]
      .unique
      .transact(xa)

  override def findAll(): IO[Seq[Entity]] =
    sql"select id, value from entity"
      .query[Entity]
      .to[List]
      .transact(xa)

  override def save(a: Entity): IO[Entity] =
    sql"insert into entity(value) values(${a.value})"
      .update
      .withUniqueGeneratedKeys[Entity]("id", "value")
      .transact(xa)

  override def update(a: Entity): IO[Int] =
    sql"update entity set value = ${a.value} where id = ${a.id}"
      .update
      .run
      .transact(xa)

  override def deleteOne(id: Long): IO[Int] =
    sql"delete from entity where id = $id"
      .update
      .run
      .transact(xa)

  override def deleteAll(): IO[Int] =
    sql"delete from entity"
      .update
      .run
      .transact(xa)

  def findFirst5(): IO[Seq[Entity]] =
    sql"select id, value from entity limit 5"
      .query[Entity]
      .stream
      .take(5)
      .compile.toList
      .transact(xa)
}


object EntityRepository {
  def apply(): EntityRepository = new EntityRepository()
}