package akka.quill.sql.crud.service

import akka.quill.sql.crud.model.Entity
import akka.quill.sql.crud.configuration.DataSource.ctx
import akka.quill.sql.crud.repository.EntityRepository

import scala.util.Try

class EntityService extends Service[Entity, Long] {
  val repository = EntityRepository()

  override def findOne(id: Long): Either[Throwable, Option[Entity]] = Try {
    ctx.transaction {
      repository.findOne(id).value.unsafeRunSync()
    }
  }.toEither

  override def findAll(): Either[Throwable, Seq[Entity]] = Try {
    ctx.transaction {
      repository.findAll().unsafeRunSync()
    }
  }.toEither

  override def save(a: Entity): Either[Throwable, Entity] = Try {
    ctx.transaction {
      repository.save(a).unsafeRunSync()
    }
  }.toEither

  override def update(a: Entity): Either[Throwable, Entity] = Try {
    ctx.transaction {
      repository.update(a).unsafeRunSync()
    }
  }.toEither

  override def deleteOne(id: Long): Either[Throwable, Long] = Try {
    ctx.transaction {
      repository.deleteOne(id).unsafeRunSync()
    }
  }.toEither

  override def deleteAll(): Either[Throwable, Long] = Try {
    ctx.transaction {
      repository.deleteAll().unsafeRunSync()
    }
  }.toEither
}

object EntityService {
  def apply(): EntityService = new EntityService()
}
