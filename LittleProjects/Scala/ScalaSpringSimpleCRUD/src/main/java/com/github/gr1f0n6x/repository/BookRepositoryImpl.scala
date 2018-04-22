package com.github.gr1f0n6x.repository

import com.github.gr1f0n6x.model.Book
import org.springframework.stereotype.Component
import scalikejdbc._

@Component
class BookRepositoryImpl extends BookRepository {
  val alias = Book.syntax("book")

  override def select(id: Long): Option[Book] = DB readOnly {
    implicit session => {
      withSQL {
        QueryDSL.select
          .from(Book as alias)
          .where.eq(Book.column.id, id)
      }.map(Book(alias.resultName)).single().apply()
    }
  }

  override def select(): Seq[Book] = DB readOnly {
    implicit session => {
      withSQL {
        QueryDSL.select
          .from(Book as alias)
      }.map(Book(alias.resultName)).list().apply()
    }
  }

  override def insert(entity: Book): Unit = DB localTx  {
    implicit session => {
      withSQL {
        QueryDSL.insert.into(Book).namedValues(
          Book.column.title -> entity.title,
          Book.column.publisherId -> entity.publisherId
        )
      }.update.apply()
    }
  }

  override def insert(entities: Seq[Book]): Unit = DB localTx {
    implicit session => {
      withSQL {
        QueryDSL.insert.into(Book).namedValues(
          Book.column.title -> sqls.?,
          Book.column.publisherId -> sqls.?
        )
      }.batch(entities).apply()
    }
  }

  override def delete(id: Long): Unit = DB localTx {
    implicit session => {
      withSQL {
        QueryDSL.delete
          .from(Book)
          .where.eq(Book.column.id, id)
      }.update.apply()
    }
  }

  override def delete(ids: Seq[Long]): Unit = DB localTx {
    implicit session => {
      withSQL {
        QueryDSL.delete
          .from(Book)
          .where.eq(Book.column.id, sqls.?)
      }.batch(ids).apply()
    }
  }

  override def update(entity: Book): Unit = DB localTx {
    implicit session => {
      withSQL {
        QueryDSL.update(Book).set(
          Book.column.title -> entity.title,
          Book.column.publisherId -> entity.publisherId
        ).where.eq(Book.column.id, entity.id)
      }.update.apply()
    }
  }

  override def update(entities: Seq[Book]): Unit = DB localTx {
    implicit session => {
      withSQL {
        QueryDSL.update(Book).set(
          Book.column.title -> sqls.?,
          Book.column.publisherId -> sqls.?
        ).where.eq(Book.column.id, sqls.?)
      }.batch(entities).apply()
    }
  }
}
