package project.source

import com.typesafe.config.Config
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe


class KafkaSource(conf: Config) extends Source {
  val kafkaParams: Map[String, Object] = Map[String, Object](
    "bootstrap.servers" -> conf.getString("bootstrap.servers"),
    "key.deserializer" -> classOf[ByteArrayDeserializer],
    "value.deserializer" -> classOf[ByteArrayDeserializer],
    "group.id" -> conf.getString("group.id"),
    "schema.registry.url" -> conf.getString("schema.registry.url"),
    "auto.offset.reset" -> "latest",
    "enable.auto.commit" -> "false"
  )

  val topic: String = conf.getString("topic")

  override def stream(ssc: StreamingContext): DStream[ConsumerRecord[Array[Byte], Array[Byte]]] = KafkaUtils.createDirectStream(
    ssc,
    PreferConsistent,
    Subscribe[Array[Byte], Array[Byte]](Array(topic), kafkaParams)
  )
}

object KafkaSource {
  def apply(conf: Config): KafkaSource = new KafkaSource(conf)
}