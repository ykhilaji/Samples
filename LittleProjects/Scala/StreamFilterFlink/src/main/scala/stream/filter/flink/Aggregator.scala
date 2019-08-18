package stream.filter.flink

import org.apache.flink.api.common.functions.AggregateFunction

import scala.collection.mutable

class Aggregator extends AggregateFunction[Array[Byte], mutable.ListBuffer[Array[Byte]], Seq[Array[Byte]]] {
  override def createAccumulator(): mutable.ListBuffer[Array[Byte]] = mutable.ListBuffer[Array[Byte]]()

  override def add(value: Array[Byte], accumulator: mutable.ListBuffer[Array[Byte]]): mutable.ListBuffer[Array[Byte]] = accumulator.+=(value)

  override def getResult(accumulator: mutable.ListBuffer[Array[Byte]]): Seq[Array[Byte]] = accumulator

  override def merge(a: mutable.ListBuffer[Array[Byte]], b: mutable.ListBuffer[Array[Byte]]): mutable.ListBuffer[Array[Byte]] = a.++=(b)
}
