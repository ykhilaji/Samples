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
  ConnectionPool.singleton("jdbc:postgresql://192.168.99.100:5432/sa", "sa", "sa")
  implicit val session = AutoSession

  sql"select 'testSelect' ".foreach(x => println(x.get[String](1)))

  sql"select * from sync.entity".map(rs => Entity(rs)).list.apply().foreach(println)
}
