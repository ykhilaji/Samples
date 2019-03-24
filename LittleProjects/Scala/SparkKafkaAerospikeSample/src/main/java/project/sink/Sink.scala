package project.sink

trait Sink {
  def save(key: Array[Byte], value: Array[Byte]): Unit
}
