package stream.filter.akka.streams

import akka.kafka.ConsumerMessage.CommittableOffset

trait Message[K, V] {
  def key: K

  def value: V
}

final case class CommitableMessage[K, V](key: K, value: V, committableOffset: CommittableOffset) extends Message[K, V]

final case class SimpleKeyedMessage[K, V](key: K, value: V) extends Message[K, V]

final case class SimpleMessage[V](value: V) extends Message[Option[_], V] {
  final override def key: Option[_] = None
}
