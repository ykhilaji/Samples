package sparksamples

import org.apache.spark.sql.{SparkSession}
import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.CassandraConnector

object SparkCassandraIntegration extends App {
  val session = SparkSession
    .builder()
    .master("local")
    .config("spark.cassandra.connection.host", "localhost")
    .config("spark.cassandra.connection.port", "9042")
    .getOrCreate()
  session.sparkContext.setLogLevel("WARN")

  import session.implicits._

  implicit val cassandraConnector: CassandraConnector = CassandraConnector(session.sparkContext.getConf)
  cassandraConnector.withSessionDo { session =>
    session.execute("create keyspace if not exists sample with replication = {'class': 'SimpleStrategy', 'replication_factor': 1}")
    session.execute("create table if not exists sample.test (date text primary key, temperaturemin text, temperaturemax text)")
  }

  val df = session.read
    .format("jdbc")
    .option("url", "jdbc:postgresql://localhost:5432/postgres")
    .option("dbtable", "public.temperatures")
    .option("user", "postgres")
    .option("password", "")
    .load()

  df.show(10)

  val count = session
    .sparkContext
    .cassandraTable("sample", "test").
    cassandraCount()

  println(s"Count: $count")

  case class TestRow(date: String, temperaturemin: String, temperaturemax: String)

  df
    .limit(100)
    .map(row => TestRow(row.getString(0), row.getString(1), row.getString(2)))
    .rdd
    .saveToCassandra("sample", "test", SomeColumns("date", "temperaturemin", "temperaturemax"))

  val countAfterSaving = session
    .sparkContext
    .cassandraTable("sample", "test")
    .cassandraCount()

  println(s"Count: $countAfterSaving")

  session
    .sparkContext
    .cassandraTable("sample", "test")
    .select("date", "temperaturemin", "temperaturemin")
    .foreach(row => println(row))
}
