package repository

import model.Entity
import org.slf4j.LoggerFactory
import scalikejdbc._

class EntityRepository extends DAO[Entity] {
  implicit val session = AutoSession
  val entityAlias = Entity.syntax("entity")

  override def select(id: Long): Entity = DB readOnly {
    implicit session => {
      withSQL {
        QueryDSL.select
          .from(Entity as entityAlias)
          .where.eq(Entity.column.id, id)
      }.map(Entity(_)).single().apply().get
    }
  }

  override def selectAll(): Seq[Entity] = DB readOnly {
    implicit session => {
      withSQL {
        QueryDSL.select
          .from(Entity as entityAlias)
      }.map(Entity(_)).list().apply()
    }
  }

  override def insert(entity: Entity): Unit = DB localTx {
    implicit session => {
      withSQL {
        QueryDSL.insert.into(Entity).namedValues(
          Entity.column.value -> entity.value
        )
      }.update.apply()
    }
  }

  override def update(entity: Entity): Unit = DB localTx {
    implicit session => {
      withSQL {
        QueryDSL.update(Entity).set(
          Entity.column.value -> entity.value
        ).where.eq(Entity.column.id, entity.id)
      }.update.apply()
    }
  }

  override def delete(id: Long): Unit = DB localTx {
    implicit session => {
      withSQL {
        QueryDSL
          .delete
          .from(Entity)
          .where.eq(Entity.column.id, id)
      }.update.apply()
    }
  }
}

object EntityRepository {
  val logger = LoggerFactory.getLogger("EntityRepository")

  Class.forName("org.postgresql.Driver")

  val settings = ConnectionPoolSettings(
    initialSize = 5,
    maxSize = 10,
    connectionTimeoutMillis = 1000,
    validationQuery = "select 1"
  )

  GlobalSettings.queryCompletionListener = (query, params, count) => {
    logger.info(s"Query completed. $query, ${params mkString ","}, $count")
  }

  GlobalSettings.queryFailureListener = (query, params, error) => {
    logger.error(s"Query failed. $query, ${params mkString ","}, $error")
  }

  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
    enabled = true,
    singleLineMode = true,
    logLevel = 'INFO
  )

  ConnectionPool.singleton(
    url = "jdbc:postgresql://192.168.99.100:5432/postgres",
    user = "postgres",
    password = "",
    settings = settings)

  def apply(): EntityRepository = new EntityRepository()
}