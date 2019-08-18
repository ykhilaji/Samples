package stream.filter.flink

import model.Message

class InMemoryCache(predicate: Function[model.Message, Boolean]) extends Cache {
  override def exists(msg: Seq[Array[Byte]]): Seq[Boolean] = msg.map(MessageUtils.fromBytes).map(predicate)

  override def close(): Unit = Unit
}

object InMemoryCache {
  def apply(predicate: Function[Message, Boolean]): InMemoryCache = new InMemoryCache(predicate)
}