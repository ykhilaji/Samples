package akka.infinispan.crud.service

import akka.infinispan.crud.model.Entity
import akka.infinispan.crud.repository.{EntityRepository, Repository}
import org.apache.logging.log4j.{LogManager, Logger}

class EntityService extends Service[Long, Entity] {
  override val logger: Logger = LogManager.getLogger("entity-service")
  override val repository: Repository[Long, Entity] = EntityRepository()
}

object EntityService {
  def apply(): EntityService = new EntityService()
}