import org.apache.spark.sql.{DataFrame, SparkSession}

// nc -l 9999
object SparkStreamingAggregateToConsoleSample extends App {
  val spark: SparkSession = SparkSession
    .builder()
    .appName("sample")
    .master("local")
    .getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")

  import spark.implicits._

  val stream: DataFrame = spark
    .readStream
    .format("socket")
    .option("host", "localhost")
    .option("port", 9999)
    .load()

  val linesCount: DataFrame = stream.as[String]
    .flatMap(_.split(" "))
    .map(_.toLowerCase)
    .groupBy("value") // default column name (?)
    .count()

  linesCount
    .writeStream
    .outputMode("update") // complete, append
    .format("console")
    .start()
    .awaitTermination()
}

object SparkStreamingWindowAggregate extends App {
  val spark: SparkSession = SparkSession
    .builder()
    .appName("sample")
    .master("local")
    .getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")

  import spark.implicits._

  val stream: DataFrame = spark
    .readStream
    .format("socket")
    .option("host", "localhost")
    .option("port", 9999)
    .load()

  import org.apache.spark.sql.functions._
  import org.apache.spark.sql.types._

  val linesCount: DataFrame = stream.as[String]
    .flatMap(_.split(" "))
    .map(l => l.length)
    .groupBy(window(unix_timestamp().cast(TimestampType), "5 seconds"), $"value")
    .count()


  linesCount
    .writeStream
    .outputMode("update") // complete, append
    .format("console")
    .start()
    .awaitTermination()
}

object SparkStreamingKafkaSample extends App {
  val spark = SparkSession
    .builder()
    .appName("kafka")
    .master("local[2]")
    .getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")
  import spark.implicits._

  // read messages from kafka topic
  val input = spark
    .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "nifi")
    .load()
    .selectExpr("CAST(key as string)", "CAST(value as string)")
    .as[(String, String)]
    .writeStream
    .format("console")
    .outputMode("append")
    .start()
    .awaitTermination()
}