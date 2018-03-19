import org.apache.spark.sql.SparkSession

// https://spark.apache.org/docs/latest/sql-programming-guide.html

object BasicSample extends App {
  val ss = SparkSession
    .builder()
    .master("local")
    .getOrCreate()

  val df = ss.read.json("/Users/grifon/WORK/Samples/SparkSamples/src/main/resources/online.json")

  df.printSchema()
  df.show()
}