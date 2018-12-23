package akka.nifi.stream.postgres.repository

import akka.nifi.stream.postgres.model.UnknownEvent
import cats.data.{OptionT, Reader}
import cats.effect.IO
import scalikejdbc._

class UnknownEventRepository extends SQLRepository[UnknownEvent, Long] {
  val alias = UnknownEvent.syntax("unknownEvent")

  override def findOne(pk: Long)(implicit session: DBSession): OptionT[IO, UnknownEvent] =
    OptionT {
      IO {
        withSQL {
          select(alias.resultAll).from(UnknownEvent as alias).where.eq(UnknownEvent.column.id, pk)
        }.map(UnknownEvent(alias.resultName)(_)).single().apply()
      }
    }

  override def findAll()(implicit session: DBSession): IO[Seq[UnknownEvent]] =
    IO {
      withSQL {
        select(alias.resultAll).from(UnknownEvent as alias)
      }.map(UnknownEvent(alias.resultName)(_)).list().apply()
    }

  def findAllByEntityId(entityId: Long)(implicit session: DBSession): IO[Seq[UnknownEvent]] =
    IO {
      withSQL {
        select(alias.resultAll).from(UnknownEvent as alias).where.eq(UnknownEvent.column.entityId, entityId)
      }.map(UnknownEvent(alias.resultName)(_)).list().apply()
    }

  override def delete(pk: Long)(implicit session: DBSession): IO[Unit] =
    IO {
      withSQL {
        deleteFrom(UnknownEvent).where.eq(UnknownEvent.column.id, pk)
      }.update().apply()
    }

  override def save(e: UnknownEvent)(implicit session: DBSession): IO[UnknownEvent] =
    IO {
      val id = withSQL {
        insert.into(UnknownEvent).namedValues(
          UnknownEvent.column.eventTime -> e.eventTime,
          UnknownEvent.column.entityId -> e.entityId
        ).returning(UnknownEvent.column.id)
      }.updateAndReturnGeneratedKey().apply()
      e.copy(id = id)
    }


  override def update(e: UnknownEvent)(implicit session: DBSession): IO[UnknownEvent] =
    IO {
      withSQL {
        QueryDSL.update(UnknownEvent).set(
          UnknownEvent.column.eventTime -> e.eventTime,
          UnknownEvent.column.entityId -> e.entityId
        ).where.eq(UnknownEvent.column.id, e.id)
      }
      e
    }
}

object UnknownEventRepository {
  def apply(): UnknownEventRepository = new UnknownEventRepository()
}