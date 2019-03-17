package project.source

import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import project.model.Event

trait Source {
  def stream(ssc: StreamingContext): DStream[Event]
}
