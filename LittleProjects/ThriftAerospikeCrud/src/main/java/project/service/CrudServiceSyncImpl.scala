package project.service

import java.util

import cats.effect.IO
import crud.{CrudService, Entity}
import project.repository.Repository

import scala.collection.JavaConverters._

class CrudServiceSyncImpl(repository: Repository[IO, Entity, Long]) extends CrudService.Iface {
  override def findOne(id: Long): util.List[Entity] = repository.findOne(id).unsafeRunSync().toList.asJava

  override def findAll(): util.List[Entity] = repository.findAll().unsafeRunSync().asJava

  override def create(entity: Entity): Entity = repository.save(entity).unsafeRunSync()

  override def update(entity: Entity): Unit = repository.update(entity).unsafeRunSync()

  override def remove(id: Long): Boolean = repository.remove(id).unsafeRunSync()

  override def removeAll(): Unit = repository.removeAll().unsafeRunSync()
}

object CrudServiceSyncImpl {
  def apply(repository: Repository[IO, Entity, Long]): CrudServiceSyncImpl = new CrudServiceSyncImpl(repository)
}