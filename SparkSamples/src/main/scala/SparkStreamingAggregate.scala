import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.hadoop.io.{IntWritable, Text}
import org.apache.hadoop.mapred.TextOutputFormat
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.kafka010.{CanCommitOffsets, HasOffsetRanges, KafkaUtils}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Duration, StreamingContext}

import scala.collection.mutable


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
