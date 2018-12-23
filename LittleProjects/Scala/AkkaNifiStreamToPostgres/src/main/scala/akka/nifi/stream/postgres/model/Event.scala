package akka.nifi.stream.postgres.model

import java.time.LocalDateTime

import akka.nifi.stream.postgres.configuration.Configuration
import scalikejdbc._

case class Event(id: Long = 0, eventTime: Option[LocalDateTime] = None, entityId: Long)

object Event extends SQLSyntaxSupport[Event] {
  override def schemaName: Option[String] = Some(Configuration.schema)

  override def tableName: String = "event"

  override def columns: Seq[String] = Seq("id", "event_time", "entity_id")

  def apply(id: Long = 0, eventTime: Option[LocalDateTime] = None, entityId: Long): Event = new Event(id, eventTime, entityId)

  def apply(r: ResultName[Event])(rs: WrappedResultSet) = new Event(
    rs.long(r.id),
    rs.localDateTimeOpt(r.eventTime),
    rs.long(r.entityId),
  )
}
