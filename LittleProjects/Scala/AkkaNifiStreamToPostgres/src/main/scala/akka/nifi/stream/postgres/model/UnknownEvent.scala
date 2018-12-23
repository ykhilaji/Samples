package akka.nifi.stream.postgres.model

import java.time.LocalDateTime

import akka.nifi.stream.postgres.configuration.Configuration
import scalikejdbc._

case class UnknownEvent(id: Long = 0, eventTime: Option[LocalDateTime] = None, entityId: Long)

object UnknownEvent extends SQLSyntaxSupport[UnknownEvent] {
  override def schemaName: Option[String] = Some(Configuration.schema)

  override def tableName: String = "unknown_event"

  override def columns: Seq[String] = Seq("id", "event_time", "entity_id")

  def apply(id: Long = 0, eventTime: Option[LocalDateTime] = None, entityId: Long): UnknownEvent = new UnknownEvent(id, eventTime, entityId)

  def apply(r: ResultName[UnknownEvent])(rs: WrappedResultSet) = new UnknownEvent(
    rs.long(r.id),
    rs.localDateTimeOpt(r.eventTime),
    rs.long(r.entityId),
  )
}
