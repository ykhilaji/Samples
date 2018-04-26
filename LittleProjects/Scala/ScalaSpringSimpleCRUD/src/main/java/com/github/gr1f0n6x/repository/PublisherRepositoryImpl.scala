package com.github.gr1f0n6x.repository

import com.github.gr1f0n6x.model.Publisher
import org.springframework.stereotype.Component
import scalikejdbc._

@Component
class PublisherRepositoryImpl extends PublisherRepository {
  val alias = Publisher.syntax("publisher")

  override def select(id: Long): Option[Publisher] = DB readOnly {
    implicit session => {
      withSQL {
        QueryDSL.select
          .from(Publisher as alias)
          .where.eq(Publisher.column.id, id)
      }.map(Publisher(alias.resultName)).single().apply()
    }
  }

  override def select(): Seq[Publisher] = DB readOnly {
    implicit session => {
      withSQL {
        QueryDSL.select
          .from(Publisher as alias)
      }.map(Publisher(alias.resultName)).list().apply()
    }
  }

  override def insert(entity: Publisher): Unit = DB localTx  {
    implicit session => {
      withSQL {
        QueryDSL.insert.into(Publisher).namedValues(
          Publisher.column.name -> entity.name
        )
      }.update.apply()
    }
  }

  override def insert(entities: Seq[Publisher]): Unit = DB localTx {
    implicit session => {
      withSQL {
        QueryDSL.insert.into(Publisher).namedValues(
          Publisher.column.name -> sqls.?,
        )
      }.batch(entities).apply()
    }
  }

  override def delete(id: Long): Unit = DB localTx {
    implicit session => {
      withSQL {
        QueryDSL.delete
          .from(Publisher)
          .where.eq(Publisher.column.id, id)
      }.update.apply()
    }
  }

  override def delete(ids: Seq[Long]): Unit = DB localTx {
    implicit session => {
      withSQL {
        QueryDSL.delete
          .from(Publisher)
          .where.eq(Publisher.column.id, sqls.?)
      }.batch(ids).apply()
    }
  }

  override def update(entity: Publisher): Unit = DB localTx {
    implicit session => {
      withSQL {
        QueryDSL.update(Publisher).set(
          Publisher.column.name -> entity.name
        ).where.eq(Publisher.column.id, entity.id)
      }.update.apply()
    }
  }

  override def update(entities: Seq[Publisher]): Unit = DB localTx {
    implicit session => {
      withSQL {
        QueryDSL.update(Publisher).set(
          Publisher.column.name -> sqls.?,
        ).where.eq(Publisher.column.id, sqls.?)
      }.batch(entities).apply()
    }
  }
}
