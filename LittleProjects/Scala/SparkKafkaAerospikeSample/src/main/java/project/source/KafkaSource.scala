package project.source

import com.typesafe.config.Config
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import project.model.Event


class KafkaSource(conf: Config) extends Source {
  val kafkaParams: Map[String, Object] = Map[String, Object](
    "bootstrap.servers" -> conf.getString("bootstrap.servers"),
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[KafkaAvroDeserializer],
    "group.id" -> conf.getString("group.id"),
    "schema.registry.url" -> conf.getString("schema.registry.url"),
    "auto.offset.reset" -> "latest",
    "enable.auto.commit" -> "false"
  )

  val topic: String = conf.getString("topic")

  override def stream(ssc: StreamingContext): DStream[Event] = KafkaUtils.createDirectStream(
    ssc,
    PreferConsistent,
    Subscribe[String, GenericRecord](Array(topic), kafkaParams)
  ).map(record => {
    val gr = record.value()
    Event(
      gr.get("id").asInstanceOf[Long],
      gr.get("value").asInstanceOf[org.apache.avro.util.Utf8].toString,
      gr.get("timestamp").asInstanceOf[Long]
    )
  })
}

object KafkaSource {
  def apply(conf: Config): KafkaSource = new KafkaSource(conf)
}