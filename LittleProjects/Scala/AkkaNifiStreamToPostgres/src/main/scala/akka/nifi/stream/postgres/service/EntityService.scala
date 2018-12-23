package akka.nifi.stream.postgres.service

import akka.nifi.stream.postgres.model.Entity
import akka.nifi.stream.postgres.repository.EntityRepository

class EntityService extends SQLService[Entity, Long] {
  override val repository = EntityRepository()

  def exists(pk: Long): Either[Throwable, Boolean] = findOne(pk).map(_.isEmpty)
}

object EntityService {
  def apply(): EntityService = new EntityService()
}