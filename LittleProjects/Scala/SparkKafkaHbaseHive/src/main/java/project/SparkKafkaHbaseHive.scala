package project

import com.sksamuel.avro4s.AvroInputStream
import com.typesafe.config.ConfigFactory
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object SparkKafkaHbaseHive {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf()
      .setAppName("sparkKafkaHbaseHive")
      .setMaster("local[2]")
      .set("spark.metrics.conf.*.sink.slf4j.class", "org.apache.spark.metrics.sink.Slf4jSink")
      .set("spark.metrics.conf.*.sink.slf4j.period", "5")
      .set("spark.metrics.conf.*.sink.slf4j.unit", "seconds")
    val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sc, Duration(500))
    val conf = ConfigFactory.load()

    val source = KafkaSource.stream(ssc, conf)
    val cache = sc.broadcast(HBaseCache(conf))

    source.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        rdd.foreachPartition(partition => {
          if (partition.nonEmpty) {
            partition.foreach(data => {
              AvroInputStream.data[Entity].from(data._2).build
                .iterator
                .foreach(entity => {
                  cache.value.get(entity.id) match {
                    case None => cache.value.put(entity.id, entity)
                    case Some(_) => cache.value.remove(entity.id)
                  }
                })
            })
          }
        })
      }
      KafkaSource.onRddEnd(source, rdd)
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
