package basic

import scalikejdbc._

case class Entity(id: Long, value: String)
object Entity extends SQLSyntaxSupport[Entity] {
  override def schemaName: Option[String] = Option("sync")

  override def tableName: String = "entity"

  override def columnNames: Seq[String] = Seq("id", "value")

  def apply(rs: WrappedResultSet): Entity = new Entity(rs.get[Long]("id"), rs.get[String]("value"))
}

object ScalikeJDBC extends App {
  Class.forName("org.postgresql.Driver")

  val settings = ConnectionPoolSettings(
    initialSize = 5,
    maxSize = 10,
    connectionTimeoutMillis = 1000,
    validationQuery = "select 1"
  )

  GlobalSettings.queryCompletionListener = (query, params, count) => {
    println(s"Query completed. $query, ${params mkString ","}, $count")
  }

  GlobalSettings.queryFailureListener = (query, params, error) => {
    println(s"Query failed. $query, ${params mkString ","}, $error")
  }

  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
    enabled = true,
    singleLineMode = true,
    logLevel = 'DEBUG
  )

  ConnectionPool.singleton(
    url = "jdbc:postgresql://192.168.99.100:5432/sa",
    user = "sa",
    password = "sa",
    settings = settings)

  implicit val session = AutoSession

  sql"select 'testSelect' ".foreach(x => println(x.get[String](1)))

  sql"select * from sync.entity".map(rs => Entity(rs)).list.apply().foreach(println)

  DB readOnly {
    implicit session => sql"select 'readOnly single result'".map(rs => rs.get[String](1)).single.apply().foreach(println)
  }

  DB readOnly {
    implicit session =>
      session.fetchSize(1)
      sql"select * from sync.entity".map(rs => Entity(rs)).first.apply().foreach(println)
  }

  val id = 1
  DB localTx {
    implicit session =>
      sql"update sync.entity set value='updatedValue' where id=${id}".update().apply()
  }

  sql"select 1/0".execute().apply()
}
