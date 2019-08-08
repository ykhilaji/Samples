package org.apache.spark.sql.structured

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.OutputMode

object CustomSourceUsage {
  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession.builder()
      .appName("test")
      .master("local[*]")
      .getOrCreate()

    val stream = sparkSession
      .readStream
      .format("org.apache.spark.sql.structured.CustomSourceRegister")
      .load()

    stream
      .writeStream
      .format("console")
      .outputMode(OutputMode.Append())
      .start()
      .awaitTermination()
  }
}
