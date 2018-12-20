package akka.nifi.stream.postgres.model

import akka.nifi.stream.postgres.configuration.Configuration
import scalikejdbc._

case class Entity(id: Long, value: String)

object Entity extends SQLSyntaxSupport[Entity] {
  override def schemaName: Option[String] = Some(Configuration.schema)

  override def tableName: String = "entity"

  override def columns: Seq[String] = Seq("id", "value")

  def apply(id: Long, value: String): Entity = new Entity(id, value)

  def apply(r: ResultName[Entity])(rs: WrappedResultSet) = new Entity(
    rs.long(r.id),
    rs.string(r.value),
  )
}
