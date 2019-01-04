package com.github.gr1f0n6x.scalatra.doobie.crud.service

import com.github.gr1f0n6x.scalatra.doobie.crud.model.Entity
import com.github.gr1f0n6x.scalatra.doobie.crud.repository.EntityRepository

class EntityService extends Service[Entity, Long] {
  val repository: EntityRepository = EntityRepository()

  override def findOne(id: Long): Either[Throwable, Entity] = repository.findOne(id).attempt.unsafeRunSync()

  override def findAll(): Either[Throwable, Seq[Entity]] = repository.findAll().attempt.unsafeRunSync()

  override def save(a: Entity): Either[Throwable, Entity] = repository.save(a).attempt.unsafeRunSync()

  override def update(a: Entity): Either[Throwable, Int] = repository.update(a).attempt.unsafeRunSync()

  override def deleteOne(id: Long): Either[Throwable, Int] = repository.deleteOne(id).attempt.unsafeRunSync()

  override def deleteAll(): Either[Throwable, Int] = repository.deleteAll().attempt.unsafeRunSync()

  def findFirst5(): Either[Throwable, Seq[Entity]] = repository.findFirst5().attempt.unsafeRunSync()
}

object EntityService {
  def apply(): EntityService = new EntityService()
}