package project.service

import cats.Id
import crud.{CrudService, Entity}

import scala.collection.JavaConverters._

/**
  * Blocking api
  * @param client - blocking client
  */
class EntityServiceSync(client: CrudService.Client) extends Service[Id, Entity, Long] {
  override def findOne(id: Long): Id[Option[Entity]] = client.findOne(id).asScala.headOption

  override def findAll(): Id[Seq[Entity]] = client.findAll().asScala

  override def create(a: Entity): Id[Entity] = client.create(a)

  override def update(a: Entity): Id[Unit] = client.update(a)

  override def remove(id: Long): Id[Boolean] = client.remove(id)

  override def removeAll(): Id[Unit] = client.removeAll()
}

object EntityServiceSync {
  def apply(client: CrudService.Client): EntityServiceSync = new EntityServiceSync(client)
}