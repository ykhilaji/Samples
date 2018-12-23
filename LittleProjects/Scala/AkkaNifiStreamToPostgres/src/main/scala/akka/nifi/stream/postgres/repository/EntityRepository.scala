package akka.nifi.stream.postgres.repository

import akka.nifi.stream.postgres.model.Entity
import cats.data.OptionT
import cats.effect.IO
import scalikejdbc._

class EntityRepository extends SQLRepository[Entity, Long] {
  val alias = Entity.syntax("entity")

  override def findOne(pk: Long)(implicit session: DBSession): OptionT[IO, Entity] =
    OptionT {
      IO {
        withSQL {
          select(alias.resultAll).from(Entity as alias).where.eq(Entity.column.id, pk)
        }.map(Entity(alias.resultName)(_)).single().apply()
      }
    }


  override def findAll()(implicit session: DBSession): IO[Seq[Entity]] =
    IO {
      withSQL {
        select(alias.resultAll).from(Entity as alias)
      }.map(Entity(alias.resultName)(_)).list().apply()
    }

  override def delete(pk: Long)(implicit session: DBSession): IO[Unit] =
    IO {
      withSQL {
        deleteFrom(Entity).where.eq(Entity.column.id, pk)
      }.execute().apply()
    }

  override def save(e: Entity)(implicit session: DBSession): IO[Entity] =
    IO {
      withSQL {
        insert.into(Entity).namedValues(
          Entity.column.id -> e.id,
          Entity.column.value -> e.value,
        )
      }.update().apply()
      e
    }

  override def update(e: Entity)(implicit session: DBSession): IO[Entity] =
    IO {
      withSQL {
        QueryDSL.update(Entity).set(
          Entity.column.value -> e.value
        )
      }.update().apply()
      e
    }
}

object EntityRepository {
  def apply(): EntityRepository = new EntityRepository()
}