package project.sink
import com.typesafe.config.Config
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.LongSerializer
import project.model.EventInfo

import collection.JavaConverters._

class KafkaSink(conf: Config) extends Sink {
  @transient
  private lazy val producer: KafkaProducer[Long, EventInfo] = {
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> conf.getString("bootstrap.servers"),
      "key.serializer" -> classOf[LongSerializer],
      "value.serializer" -> classOf[KafkaAvroSerializer],
      "schema.registry.url" -> conf.getString("schema.registry.url")
    )
    new KafkaProducer[Long, EventInfo](kafkaParams.asJava)
  }

  private val topic = conf.getString("topic")

  override def save(o: EventInfo): Unit = producer.send(
    new ProducerRecord[Long, EventInfo](topic, o.id, o), new ProducerCallback
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