package com.github.gr1f0n6x.model

import scalikejdbc._

case class Author(
                 id: Option[Long],
                 firstName: String,
                 lastName: Option[String]
                 )

object Author extends SQLSyntaxSupport[Author] {
  override def schemaName = Option[String]("crud")

  override def tableName = "author"

  override def columns = Seq("id", "first_name", "last_name")

  override def nameConverters = Map("^id$" -> "id", "^firstName$" -> "first_name", "^lastName$" -> "last_name")

  def apply(e: ResultName[Author])(rs: WrappedResultSet): Author = new Author(
    id = rs.longOpt(e.id),
    firstName = rs.string(e.firstName),
    lastName = rs.stringOpt(e.lastName)
  )
}
