package com.github.gr1f0n6x.repository

import com.github.gr1f0n6x.model.Author
import org.springframework.stereotype.Component
import scalikejdbc._

@Component
class AuthorRepositoryImpl extends AuthorRepository {
  val alias = Author.syntax("author")

  override def select(id: Long): Option[Author] = DB readOnly {
    implicit session => {
      withSQL {
        QueryDSL.select
          .from(Author as alias)
          .where.eq(Author.column.id, id)
      }.map(Author(alias.resultName)).single().apply()
    }
  }

  override def select(): Seq[Author] = DB readOnly {
    implicit session => {
      withSQL {
        QueryDSL.select
          .from(Author as alias)
      }.map(Author(alias.resultName)).list().apply()
    }
  }

  override def insert(entity: Author): Unit = DB localTx  {
    implicit session => {
      withSQL {
        QueryDSL.insert.into(Author).namedValues(
          Author.column.firstName -> entity.firstName,
          Author.column.lastName -> entity.lastName
        )
      }.update.apply()
    }
  }

  override def insert(entities: Seq[Author]): Unit = DB localTx {
    implicit session => {
      withSQL {
        QueryDSL.insert.into(Author).namedValues(
          Author.column.firstName -> sqls.?,
          Author.column.lastName -> sqls.?
        )
      }.batch(entities).apply()
    }
  }

  override def delete(id: Long): Unit = DB localTx {
    implicit session => {
      withSQL {
        QueryDSL.delete
          .from(Author)
          .where.eq(Author.column.id, id)
      }.update.apply()
    }
  }

  override def delete(ids: Seq[Long]): Unit = DB localTx {
    implicit session => {
      withSQL {
        QueryDSL.delete
          .from(Author)
          .where.eq(Author.column.id, sqls.?)
      }.batch(ids).apply()
    }
  }

  override def update(entity: Author): Unit = DB localTx {
    implicit session => {
      withSQL {
        QueryDSL.update(Author).set(
          Author.column.firstName -> entity.firstName,
          Author.column.lastName -> entity.lastName
        ).where.eq(Author.column.id, entity.id)
      }.update.apply()
    }
  }

  override def update(entities: Seq[Author]): Unit = DB localTx {
    implicit session => {
      withSQL {
        QueryDSL.update(Author).set(
          Author.column.firstName -> sqls.?,
          Author.column.lastName -> sqls.?
        ).where.eq(Author.column.id, sqls.?)
      }.batch(entities).apply()
    }
  }
}
