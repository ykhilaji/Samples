package project

import com.typesafe.config.ConfigFactory
import org.apache.logging.log4j.LogManager
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.streaming.kafka010.{CanCommitOffsets, HasOffsetRanges}
import org.apache.spark.{SparkConf, SparkContext}
import project.action.MergeEventState
import project.cache.AerospikeCache
import project.sink.KafkaSink
import project.source.KafkaSource

object SparkKafkaAerospikeSample {
  def main(args: Array[String]): Unit = {
    val logger = LogManager.getLogger("project")
    val config = ConfigFactory.load().getConfig("project")
    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("SparkKafkaAerospikeSample")
    logger.info("Creating spark context")
    val sc = new SparkContext(sparkConf)
    sc.setLogLevel("WARN")
    logger.info("Creating spark streaming context")
    val ssc = new StreamingContext(sc, Duration(500))

    val broadcastedConfig = sc.broadcast(config)

    logger.info("Creating kafka source")
    val source = KafkaSource(broadcastedConfig.value.getConfig("kafka.consumer"))
    val stream = source.stream(ssc)

    stream.foreachRDD(rdd => {
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

      rdd.foreachPartition(partition => {
        val cache = AerospikeCache(broadcastedConfig.value.getConfig("aerospike"))
        val action = MergeEventState()
        val sink = KafkaSink(broadcastedConfig.value.getConfig("kafka.producer"))

        partition.foreach(event => {
          val cachedEventInfo = cache.get(event.id)
          val eventInfo = action.process(event, cachedEventInfo)
          cache.put(eventInfo.id, eventInfo)
          sink.save(eventInfo)
        })
      })

      // commit on rdd end
      stream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
    })

    logger.info("Start")
    ssc.start()
    ssc.awaitTermination()
  }
}
