package project

import com.typesafe.config.Config
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010.{CanCommitOffsets, HasOffsetRanges, KafkaUtils}
import org.apache.spark.streaming.kafka010.LocationStrategies._
import org.apache.spark.streaming.kafka010.ConsumerStrategies._

object KafkaSource {
  def stream(ssc: StreamingContext, config: Config): DStream[(Array[Byte], Array[Byte])] = {
    val params = Map[String, Object](
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "false",
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> config.getString("broker.servers"),
      ConsumerConfig.GROUP_ID_CONFIG -> config.getString("group.id"),
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[ByteArrayDeserializer],
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[ByteArrayDeserializer],
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest"
    )

    KafkaUtils.createDirectStream(
      ssc,
      PreferConsistent,
      Subscribe[Array[Byte], Array[Byte]](Array(config.getString("source.topic")), params)
    ).map(r => (r.key(), r.value()))
  }

  def onRddEnd(stream: DStream[_], rdd: RDD[_]): Unit = (stream, rdd) match {
    case (s: CanCommitOffsets, r: HasOffsetRanges) => s.commitAsync(r.offsetRanges)
    case _ => Unit
  }
}
