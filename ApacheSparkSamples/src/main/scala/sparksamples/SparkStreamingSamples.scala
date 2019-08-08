package sparksamples

import org.apache.spark.sql.types.{StructType, TimestampType}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

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

object SparkStructuredStreamingKafkaToHiveSample extends App {
  val spark = SparkSession
    .builder()
    .config("hive.metastore.uris", "thrift://localhost:9083")
    .config("hive.exec.dynamic.partition.mode", "nonstrict")
    .enableHiveSupport()
    .appName("kafka")
    .master("local[2]")
    .getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")

  import spark.implicits._

  val schema = new StructType()
    .add($"id".int)
    .add($"value".string)
    .add($"timestamp".long)
    .add($"random".long)

  // read messages from kafka topic
  // consumer group id generated automatically
  val input = spark
    .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "nifi")
    .load()
    .selectExpr("CAST(key as string)", "CAST(value as string)")
    .as[(String, String)]
    .select(current_timestamp().as("now"), from_json($"value", schema) as "data")
    .select("now", "data.*")
    .withWatermark("now", "1 seconds")
    .groupBy(
      window($"now", "10 seconds", "10 seconds"),
      $"id"
    )
    .agg(
      first(to_date($"now")).as("now"),
      count($"random").as("count"),
      sum($"random").as("sum"),
      collect_list($"value").as("values")
    )
    .select($"sum", $"count", $"values", $"now", $"id")
    .writeStream
    .foreachBatch { (dataSet, batchId) =>
      dataSet
        .toDF()
        .write
        .mode("append")
        .insertInto("nifi")
    }
    .start()
    .awaitTermination()
}