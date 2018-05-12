import org.apache.spark.sql.{SaveMode, SparkSession}

// https://spark.apache.org/docs/latest/sql-programming-guide.html

object BasicSample extends App {
  val ss = SparkSession
    .builder()
    .master("local")
    .getOrCreate()

  ss.sparkContext.setLogLevel("WARN")

  val df = ss.read.json("/Users/grifon/WORK/Samples/SparkSamples/src/main/resources/online.json")

  df.printSchema()
  df.show()
}

object SaveAsParquet extends App {
  val ss = SparkSession
    .builder()
    .master("local")
    .getOrCreate()

  ss.sparkContext.setLogLevel("WARN")

  val df = ss.read.json("/Users/grifon/WORK/Samples/SparkSamples/src/main/resources/online.json")

  df.select("label", "peak24").write.save("/Users/grifon/WORK/Samples/SparkSamples/src/main/resources/output.parquet")
}

object SelectUsingDSL extends App {
  val ss = SparkSession
    .builder()
    .master("local")
    .getOrCreate()

  ss.sparkContext.setLogLevel("WARN")


  import ss.implicits._

  val df = ss.read.json("/Users/grifon/WORK/Samples/SparkSamples/src/main/resources/online.json")
  df.select("label").show()
  df.select("label", "count").where($"label".startsWith("P")).show() // $ -> shortcut for new ColumnName
  df.filter($"count" > 10000).show()
}

object DataFrameAsCaseClass extends App {
  val ss = SparkSession
    .builder()
    .master("local")
    .getOrCreate()

  ss.sparkContext.setLogLevel("WARN")

  case class Info(label: String, count: Long, peak24: Long)

  import ss.implicits._

  val df = ss.read.json("/Users/grifon/WORK/Samples/SparkSamples/src/main/resources/online.json").as[Info]
  df.show()
}

object SelectUsingSQL extends App {
  val ss = SparkSession
    .builder()
    .master("local")
    .getOrCreate()

  ss.sparkContext.setLogLevel("WARN")

  val df = ss
    .read
    .option("sep", ",")
    .option("header", "true")
    .csv("/Users/grifon/WORK/Samples/SparkSamples/src/main/resources/sample.csv")

  df.createOrReplaceTempView("csv")

  df.printSchema()
  ss.sql("select count(1) from csv").show()
  ss.sql("select * from csv limit 5").show()
  ss.sql("select fog, count(1) from csv group by fog").show()
}

object SaveAsPersistentTable extends App {
  val ss = SparkSession
    .builder()
    .master("local")
    .getOrCreate()

  ss.sparkContext.setLogLevel("WARN")

  val df = ss
    .read
    .option("sep", ",")
    .option("header", "true")
    .csv("/Users/grifon/WORK/Samples/SparkSamples/src/main/resources/sample.csv")


  df
    .select("date", "temperaturemin", "temperaturemax")
    .write
    .mode(SaveMode.ErrorIfExists)
    .format("jdbc")
    .option("url", "jdbc:postgresql://localhost:5432/postgres")
    .option("user", "postgres")
    .option("password", "")
    .option("dbtable", "public.temperatures")
    .option("createTableColumnTypes", "  date varchar(255), temperaturemin varchar(255), temperaturemax varchar(255)")
    .save()
}