import org.apache.spark.{SparkConf, SparkContext}

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