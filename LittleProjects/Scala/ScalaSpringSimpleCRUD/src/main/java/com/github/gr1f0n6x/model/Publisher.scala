package com.github.gr1f0n6x.model

import scalikejdbc._

case class Publisher(id: Option[Long],
                     name: String
                    )

object Publisher extends SQLSyntaxSupport[Publisher] {
  override def schemaName = Option[String]("crud")

  override def tableName = "publisher"

  override def columns = Seq("id", "name")

  override def nameConverters = Map("^id$" -> "id", "^name$" -> "name")

  def apply(e: ResultName[Publisher])(rs: WrappedResultSet): Publisher = new Publisher(
    id = rs.longOpt(e.id),
    name = rs.string(e.name)
  )
}
