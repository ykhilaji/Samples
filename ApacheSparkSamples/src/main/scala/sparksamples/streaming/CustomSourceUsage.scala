package sparksamples.streaming

import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object CustomSourceUsage {
  def main(args: Array[String]): Unit = {
    val config = new SparkConf()
      .setMaster("local[*]")
      .setAppName("customSource")
    val spark = new SparkContext(config)
    val ssc = new StreamingContext(spark, Duration(1000)) // 1 second

//    val stream = ssc.receiverStream(new CustomSource(100)) // 0.1 second
    val stream = new CustomSourceUsingInputDStream(ssc, Map("delay" -> "100"))
    stream
        .filter(_ % 2 == 0)
        .print()

    ssc.start()
    ssc.awaitTerminationOrTimeout(10000) // 10 seconds
  }
}
