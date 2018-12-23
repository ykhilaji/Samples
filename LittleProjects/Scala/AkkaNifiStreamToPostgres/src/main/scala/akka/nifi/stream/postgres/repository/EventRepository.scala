package akka.nifi.stream.postgres.repository

import akka.nifi.stream.postgres.model.Event
import cats.data.{OptionT, Reader}
import cats.effect.IO
import scalikejdbc._

class EventRepository extends SQLRepository[Event, Long] {
  val alias = Event.syntax("event")

  override def findOne(pk: Long)(implicit session: DBSession): OptionT[IO, Event] =
    OptionT {
      IO {
        withSQL {
          select(alias.resultAll).from(Event as alias).where.eq(Event.column.id, pk)
        }.map(Event(alias.resultName)(_)).single().apply()
      }
    }

  override def findAll()(implicit session: DBSession): IO[Seq[Event]] =
    IO {
      withSQL {
        select(alias.resultAll).from(Event as alias)
      }.map(Event(alias.resultName)(_)).list().apply()
    }

  def findAllByEntityId(entityId: Long)(implicit session: DBSession): IO[Seq[Event]] =
    IO {
      withSQL {
        select(alias.resultAll).from(Event as alias).where.eq(Event.column.entityId, entityId)
      }.map(Event(alias.resultName)(_)).list().apply()
    }

  override def delete(pk: Long)(implicit session: DBSession): IO[Unit] =
    IO {
      withSQL {
        deleteFrom(Event).where.eq(Event.column.id, pk)
      }.update().apply()
    }

  override def save(e: Event)(implicit session: DBSession): IO[Event] =
    IO {
      val id = withSQL {
        insert.into(Event).namedValues(
          Event.column.eventTime -> e.eventTime,
          Event.column.entityId -> e.entityId
        ).returning(Event.column.id)
      }.updateAndReturnGeneratedKey().apply()
      e.copy(id = id)
    }


  override def update(e: Event)(implicit session: DBSession): IO[Event] =
    IO {
      withSQL {
        QueryDSL.update(Event).set(
          Event.column.eventTime -> e.eventTime,
          Event.column.entityId -> e.entityId
        ).where.eq(Event.column.id, e.id)
      }
      e
    }
}

object EventRepository {
  def apply(): EventRepository = new EventRepository()
}