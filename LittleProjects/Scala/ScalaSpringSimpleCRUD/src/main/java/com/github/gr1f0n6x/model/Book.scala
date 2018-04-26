package com.github.gr1f0n6x.model

import scalikejdbc._

case class Book(id: Option[Long],
                title: String,
                publisherId: Option[Long]
               )

object Book extends SQLSyntaxSupport[Book] {
  override def schemaName = Option[String]("crud")

  override def tableName = "book"

  override def columns = Seq("id", "title", "publisher_id")

  override def nameConverters = Map("^id$" -> "id", "^title$" -> "title", "^publisherId$" -> "publisher_id")

  def apply(e: ResultName[Book])(rs: WrappedResultSet): Book = new Book(
    id = rs.longOpt(e.id),
    title = rs.string(e.title),
    publisherId = rs.longOpt(e.publisherId)
  )
}
