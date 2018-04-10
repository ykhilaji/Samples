package service

import model.{Entity, Flux, Mono}
import repository.EntityRepository

class EntityService extends Service[Entity] {
  lazy val entityRepository = EntityRepository()

  override def select(id: Long): Mono[Entity] = Mono(entityRepository.select(id))

  override def selectAll(): Flux[Entity] = Flux(entityRepository.selectAll())

  override def insert(entity: Entity): Mono[Entity] = {
    entityRepository.insert(entity)
    Mono(entity)
  }

  override def update(entity: Entity): Mono[Entity] = {
    entityRepository.update(entity)
    Mono(entity)
  }

  override def delete(id: Long): Mono[Entity] = {
    entityRepository.delete(id)
    Mono(Entity())
  }
}

object EntityService {
  def apply(): EntityService = new EntityService()
}
