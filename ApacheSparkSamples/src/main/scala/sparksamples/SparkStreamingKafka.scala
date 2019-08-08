package sparksamples

import java.sql.{Date, Timestamp}
import java.time.{Instant, ZoneId}
import java.util

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapred.TextOutputFormat
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, Deserializer, StringDeserializer}
import org.apache.log4j.LogManager
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.sql.functions._

import scala.collection.mutable

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

object SparkStreamingAggregate {
  val mapper = new ObjectMapper()

  case class Message(
                      id: Int,
                      value: String,
                      timestamp: Long,
                      random: Long,
                      aggregate: Aggregate = Aggregate()) extends Serializable

  case class Aggregate(
                        values: mutable.Map[String, Long] = mutable.Map(),
                        var sum: Long = 0,
                        var count: Long = 0) extends Serializable

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("aggregate")
    val sc = SparkContext.getOrCreate(conf)
    sc.setLogLevel("INFO")
    val ssc = new StreamingContext(sc, Duration(1000))
    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092",
      ConsumerConfig.GROUP_ID_CONFIG -> "aggregate",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[ByteArrayDeserializer].getName,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[ByteArrayDeserializer].getName,
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest"
    )

    val stream = KafkaUtils.createDirectStream(ssc, PreferConsistent, Subscribe[Array[Byte], Array[Byte]](Array("nifi"), kafkaParams))

    val aggregate: DStream[(Int, Message)] = stream
      .map(record => {
        val tree = mapper.readTree(record.value())
        val message = Message(
          tree.get("id").asInt(),
          tree.get("value").asText(),
          tree.get("timestamp").asLong(),
          tree.get("random").asLong())

        (message.id, message)
      })
      .reduceByKeyAndWindow({
        case (left, right) =>
          left.aggregate.sum += left.random + right.random
          left.aggregate.count += 1
          left.aggregate.values.update(left.value, left.aggregate.values.getOrElse(left.value, 0L) + 1L)
          left.aggregate.values.update(right.value, left.aggregate.values.getOrElse(right.value, 0L) + 1L)

          left
      }, Duration(30000))

    saveAsHadoopFile(aggregate)

    ssc.start()
    ssc.awaitTermination()
  }

  //single file
  //overwrite
  def saveAsTextFile(stream: DStream[(Int, Message)]) = {
    stream.foreachRDD(rdd => {
      rdd.saveAsTextFile("/Users/grifon/WORK/Samples/SparkSamples/src/main/resources")
    })
  }

  //start-dsf.sh
  //single file
  def saveAsHadoopFile(stream: DStream[(Int, Message)]) = {
    stream.foreachRDD(rdd => {
      rdd.map {
        case (k, v) => (k.toString, v)
      }.saveAsHadoopFile("hdfs://localhost:8020/aggregate", classOf[Text], classOf[Text], classOf[TextOutputFormat[Text, Text]])
    })
  }
}

object SparkStreamingKafkaToHive extends App {
  val logger = LogManager.getLogger("kafka")

  case class Message(id: Int, value: String, timestamp: Date, random: Long)

  class KafkaMessageDeserializer extends Deserializer[Message] {
    val mapper = new ObjectMapper()

    override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = Unit

    override def deserialize(topic: String, data: Array[Byte]): Message = {
      if (data == null) {
        null
      } else {
        try {
          val tree = mapper.readTree(data)
          Message(
            tree.get("id").asInt(),
            tree.get("value").asText(),
            Date.valueOf(Instant.ofEpochMilli(tree.get("timestamp").asLong()).atZone(ZoneId.systemDefault()).toLocalDate),
            tree.get("random").asLong())
        } catch {
          case e: Exception => throw new RuntimeException(e)
        }
      }
    }

    override def close(): Unit = Unit
  }

  val conf = new SparkConf().setAppName("kafka").setMaster("local[2]")
  val sc = new SparkContext(conf)
  sc.setLogLevel("ERROR")
  val ssc = new StreamingContext(sc, Duration(1000))

  val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> "localhost:9092",
    "key.deserializer" -> classOf[ByteArrayDeserializer],
    "value.deserializer" -> classOf[KafkaMessageDeserializer],
    "group.id" -> "kafka_spark",
    "auto.offset.reset" -> "earliest",
    "enable.auto.commit" -> "true" // for testing
  )
  val stream = KafkaUtils.createDirectStream(ssc, PreferConsistent, Subscribe[Array[Byte], Message](Array("nifi"), kafkaParams))

  val window: DStream[(Int, Message)] = stream
    .map(record => record.value())
    .map(m => (m.id, m))
    .window(Duration(15000), Duration(15000))

  window
    .foreachRDD(rdd => {
      val spark = SparkSession.builder().config(rdd.sparkContext.getConf).getOrCreate()
      import spark.implicits._

      rdd
        .repartition(5)
        .map(_._2)
        .toDF()
        .select($"id", $"value", $"timestamp", $"random")
        .groupBy("id")
        .agg(
          first($"id"),
          first($"timestamp").as("timestamp"),
          count($"value"),
          sum($"random")
        )
        .write
        .mode("append")
        //        .partitionBy("timestamp")
        .csv("/Users/grifon/WORK/Samples/SparkSamples/src/main/resources/output")
    })

  ssc.start()
  ssc.awaitTermination()
}

// print aggregated result each 10 seconds
object SparkStreamingKafkaWindowOutput extends App {
  val logger = LogManager.getLogger("kafka")

  sealed trait KafkaMessage

  case class Message(id: Int, value: String, timestamp: Timestamp, random: Long) extends KafkaMessage {
    override def hashCode(): Int = id.hashCode()

    override def equals(obj: Any): Boolean = obj match {
      case m: Message => id == m.id
      case _ => false
    }
  }

  case object Empty extends KafkaMessage

  case class Aggregate(sum: Long, count: Long, values: List[String])

  class KafkaMessageDeserializer extends Deserializer[KafkaMessage] {
    val mapper = new ObjectMapper()

    override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = Unit

    override def deserialize(topic: String, data: Array[Byte]): KafkaMessage = {
      if (data == null) {
        Empty
      } else {
        try {
          val tree = mapper.readTree(data)
          Message(
            tree.get("id").asInt(),
            tree.get("value").asText(),
            Timestamp.valueOf(Instant.ofEpochMilli(tree.get("timestamp").asLong()).atZone(ZoneId.systemDefault()).toLocalDateTime),
            tree.get("random").asLong())
        } catch {
          case e: Exception => throw new RuntimeException(e)
        }
      }
    }

    override def close(): Unit = Unit
  }

  val conf = new SparkConf().setAppName("kafka").setMaster("local[2]")
  val sc = new SparkContext(conf)
  sc.setLogLevel("ERROR")
  val ssc = new StreamingContext(sc, Duration(1000))

  val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> "localhost:9092",
    "key.deserializer" -> classOf[ByteArrayDeserializer],
    "value.deserializer" -> classOf[KafkaMessageDeserializer],
    "group.id" -> "kafka_spark",
    "auto.offset.reset" -> "earliest",
    "enable.auto.commit" -> "true" // for testing
  )

  val stream = KafkaUtils.createDirectStream(ssc, PreferConsistent, Subscribe[Array[Byte], KafkaMessage](Array("nifi"), kafkaParams))
  stream
    .map(_.value())
    .filter {
      case Empty => false
      case _ => true
    }
    .map[(Message, Aggregate)] {
    case m: Message => (m, Aggregate(m.random, 1, List(m.value)))
  }
    .reduceByKeyAndWindow((left: Aggregate, right: Aggregate) => {
      Aggregate(left.sum + right.sum, left.count + right.count, left.values ::: right.values)
    }, Duration(10000), Duration(10000), 2)
    .print()

  ssc.start()
  ssc.awaitTermination()
}