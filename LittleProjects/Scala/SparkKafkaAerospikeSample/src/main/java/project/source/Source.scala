package project.source

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

trait Source {
  def stream(ssc: StreamingContext): DStream[ConsumerRecord[Array[Byte], Array[Byte]]]
}
