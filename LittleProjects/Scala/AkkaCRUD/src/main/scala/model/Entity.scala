package model

import scalikejdbc._

case class Entity(id: Long = 0, var value: String = "")

object Entity extends SQLSyntaxSupport[Entity] {
  override def schemaName: Option[String] = Option("akka")

  override def tableName: String = "entity"

  override def columnNames: Seq[String] = Seq("id", "value")

  def apply(rs: WrappedResultSet): Entity = Entity(rs.get[Long](1), rs.get[String](2))
}