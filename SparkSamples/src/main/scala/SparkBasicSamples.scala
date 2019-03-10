import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext, sql}

object WordCount extends App {
  val input = "/Users/grifon/WORK/Samples/SparkSamples/src/main/resources/input.txt"
  val output = "/Users/grifon/WORK/Samples/SparkSamples/src/main/resources/output"
  val conf = new SparkConf()
    .setAppName("wordCount")
    .setMaster("local")

  val sc = new SparkContext(conf)

  val rdd = sc.textFile(input)

  val wordCount = rdd
    .flatMap(line => line.split(" "))
    .map(word => (word, 1))
    .reduceByKey(_ + _)

  wordCount.saveAsTextFile(output)
}

object WordCountSortDesc extends App {
  val input = "/Users/grifon/WORK/Samples/SparkSamples/src/main/resources/input.txt"
  val output = "/Users/grifon/WORK/Samples/SparkSamples/src/main/resources/output"
  val conf = new SparkConf()
    .setAppName("sort")
    .setMaster("local")

  val sc = new SparkContext(conf)

  val rdd = sc.textFile(input)

  rdd
    .flatMap(line => line.split(" "))
    .map(word => (word, 1))
    .reduceByKey(_ + _)
    .sortBy(_._2, ascending = false)
    .saveAsTextFile(output)
}

object GroupByWordCount extends App {
  val input = "/Users/grifon/WORK/Samples/SparkSamples/src/main/resources/input.txt"
  val output = "/Users/grifon/WORK/Samples/SparkSamples/src/main/resources/output"
  val conf = new SparkConf()
    .setAppName("sortByWordCount")
    .setMaster("local")

  val sc = new SparkContext(conf)

  val rdd = sc.textFile(input)

  rdd
    .flatMap(line => line.split(" "))
    .map(word => (word, 1))
    .reduceByKey(_ + _)
    .map(f => (f._2, f._1))
    .reduceByKey(_ + ", " + _)
    .saveAsTextFile(output)
}

object CsvFileIntoExistingPartitionedHiveTable extends App {
  val input = "/Users/grifon/WORK/Samples/SparkSamples/src/main/resources/SalesJan2009.csv"
  val output = "hdfs://localhost:8020/output"

  val conf = new SparkConf().setAppName("csv").setMaster("local[2]")
  val spark = new sql.SparkSession.Builder()
    .config(conf)
    .config("hive.metastore.uris", "thrift://localhost:9083")
    .config("hive.exec.dynamic.partition.mode", "nonstrict")
    .enableHiveSupport()
    .getOrCreate()

  import spark.implicits._
  import org.apache.spark.sql.functions._
  import org.apache.spark.sql.types._

  val data: DataFrame = spark
    .read
    .format("csv")
    .option("header", "true")
    .option("delimiter", ",")
    .load(input)
    .withColumn("price", $"price".cast(LongType))
    .withColumn("product", trim($"product"))
    .withColumn("latitude", $"latitude".cast(DoubleType))
    .withColumn("longitude", $"longitude".cast(DoubleType))
    .withColumn("transaction_date", to_timestamp($"transaction_date", "MM/dd/yy HH:mm").cast(TimestampType))
    .withColumn("account_created", to_timestamp($"account_created", "MM/dd/yy HH:mm").cast(TimestampType))
    .withColumn("last_login", to_timestamp($"last_login", "MM/dd/yy HH:mm").cast(TimestampType))

  data.persist(StorageLevel.MEMORY_ONLY)
  data.printSchema()
  data.show(50)
  val maxPrice = data.select(max($"price"))
  val countByProductType = data.groupBy($"product").count()
  val statisticByDateAndProduct = data.groupBy(to_date($"transaction_date"), $"payment_type", $"product")
    .agg(max($"price"), avg($"price"), count($"price"), approx_count_distinct($"price"))


  maxPrice.show()
  countByProductType.show()
  statisticByDateAndProduct.show()

  //set hive.exec.dynamic.partition.mode=nonstrict
  data
    .withColumn("transaction_date", to_date($"transaction_date"))
    .groupBy($"transaction_date", $"payment_type", $"product")
    .agg(
      max($"price").as("max_price"),
      min($"price").as("min_price"),
      sum($"price").as("sum"),
      avg($"price").as("average"),
      count($"price").as("count")
    )
    .select($"max_price", $"min_price", $"sum", $"average", $"count", $"transaction_date", $"payment_type", $"product")
    .write
//    .partitionBy("transaction_date", "payment_type", "product") // - data will be dynamically partitioned
    .mode("append")
    .format("parquet")
    .insertInto("aggregates")
}