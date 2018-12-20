package akka.nifi.stream.postgres.repository

import akka.nifi.stream.postgres.model.Entity
import cats.data.{OptionT, Reader}
import cats.effect.IO
import scalikejdbc._

class EntityRepository extends SQLRepository[Entity, Long] {
  val alias = Entity.syntax("entity")

  override def findOne(pk: Long): Reader[DBSession, OptionT[IO, Entity]] = Reader[DBSession, OptionT[IO, Entity]] {
    implicit session => {
      OptionT {
        IO {
          withSQL {
            select(alias.resultAll).from(Entity as alias).where.eq(Entity.column.id, pk)
          }.map(Entity(alias.resultName)(_)).single().apply()
        }
      }
    }
  }


  override def findAll(): Reader[DBSession, IO[Seq[Entity]]] = Reader[DBSession, IO[Seq[Entity]]] {
    implicit session => {
      IO {
        withSQL {
          select(alias.resultAll).from(Entity as alias)
        }.map(Entity(alias.resultName)(_)).list().apply()
      }
    }
  }

  override def delete(pk: Long): Reader[DBSession, IO[Unit]] = Reader[DBSession, IO[Unit]] {
    implicit session => {
      IO {
        withSQL {
          deleteFrom(Entity).where.eq(Entity.column.id, pk)
        }.execute().apply()
      }
    }
  }

  override def save(e: Entity): Reader[DBSession, IO[Entity]] = Reader[DBSession, IO[Entity]] {
    implicit session => {
      IO {
        withSQL {
          insert.into(Entity).namedValues(
            Entity.column.id -> e.id,
            Entity.column.value -> e.value,
          )
        }.update().apply()
        e
      }
    }
  }

  override def update(e: Entity): Reader[DBSession, IO[Entity]] = Reader[DBSession, IO[Entity]] {
    implicit session => {
      IO {
        withSQL {
          QueryDSL.update(Entity).set(
            Entity.column.value -> e.value
          )
        }.update().apply()
        e
      }
    }
  }
}

object EntityRepository {
  def apply(): EntityRepository = new EntityRepository()
}