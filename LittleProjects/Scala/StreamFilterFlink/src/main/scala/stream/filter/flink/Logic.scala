package stream.filter.flink

import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger
import org.apache.flink.streaming.api.scala._
import TypeInfo._
import model.Message


object Logic {
  def filter(stream: DataStream[Array[Byte]], filterImpl: ProcessFunction[Seq[Array[Byte]], Seq[Array[Byte]]]): DataStream[Message] =
    stream
      .windowAll(TumblingEventTimeWindows.of(Time.seconds(1)))
      .trigger(CountTrigger.of(10)) // need custom trigger to be able to trigger by count and time
      .aggregate(new Aggregator())
      .process(filterImpl)
      .flatMap(identity[Seq[Array[Byte]]](_))
      .map(MessageUtils.fromBytes(_))
}
