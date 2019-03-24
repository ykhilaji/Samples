package project.sink

import com.typesafe.config.Config
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.ByteArraySerializer

import collection.JavaConverters._

class KafkaSink(conf: Config) extends Sink {
  @transient
  private lazy val producer: KafkaProducer[Array[Byte], Array[Byte]] = {
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> conf.getString("bootstrap.servers"),
      "key.serializer" -> classOf[ByteArraySerializer],
      "value.serializer" -> classOf[ByteArraySerializer],
      "schema.registry.url" -> conf.getString("schema.registry.url")
    )
    new KafkaProducer[Array[Byte], Array[Byte]](kafkaParams.asJava)
  }

  private val topic = conf.getString("topic")

  override def save(key: Array[Byte], value: Array[Byte]): Unit = producer.send(
    new ProducerRecord[Array[Byte], Array[Byte]](topic, key, value), new ProducerCallback
  )

  private class ProducerCallback extends Callback {
    override def onCompletion(recordMetadata: RecordMetadata, e: Exception): Unit = {
      if (e != null) {

      }
    }
  }
}

object KafkaSink {
  def apply(conf: Config): KafkaSink = new KafkaSink(conf)
}