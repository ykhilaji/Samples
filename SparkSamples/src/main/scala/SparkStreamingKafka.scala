import java.util

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object SparkStreamingKafka extends App {
  val conf = new SparkConf().setAppName("kafka").setMaster("local")
  val sc = SparkContext.getOrCreate(conf)
  sc.setLogLevel("ERROR")

  val ssc = new StreamingContext(sc, Duration(1000))

  val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> "localhost:29092",
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    "group.id" -> "kafka_spark",
    "auto.offset.reset" -> "earliest",
    "enable.auto.commit" -> "false"
  )

  val stream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils
    .createDirectStream(ssc,
      PreferConsistent,
      Subscribe[String, String](Array("nifi"), kafkaParams))

  stream.map(_.value()).print()

  ssc.start()
  ssc.awaitTermination()
}

object SparkStreamingKafkaJson extends App {

  class JsonDeserializer extends Deserializer[JsonNode] {
    val mapper = new ObjectMapper()

    override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = Unit

    override def deserialize(topic: String, data: Array[Byte]): JsonNode = {
      if (data == null) {
        mapper.createObjectNode()
      } else {
        try {
          mapper.readTree(data)
        } catch {
          case e: Exception => throw new RuntimeException(e)
        }
      }
    }

    override def close(): Unit = Unit
  }

  val conf = new SparkConf().setAppName("kafka").setMaster("local")
  val sc = SparkContext.getOrCreate(conf)
  sc.setLogLevel("ERROR")

  val ssc = new StreamingContext(sc, Duration(1000))

  val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> "localhost:29092",
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[JsonDeserializer],
    "group.id" -> "kafka_spark",
    "auto.offset.reset" -> "earliest",
    "enable.auto.commit" -> "false"
  )

  val stream: InputDStream[ConsumerRecord[String, JsonNode]] = KafkaUtils
    .createDirectStream(ssc,
      PreferConsistent,
      Subscribe[String, JsonNode](Array("nifi"), kafkaParams))

  stream.map(_.value().toString).print()

  ssc.start()
  ssc.awaitTermination()
}
