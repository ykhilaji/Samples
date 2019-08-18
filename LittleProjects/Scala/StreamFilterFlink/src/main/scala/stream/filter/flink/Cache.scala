package stream.filter.flink


trait Cache extends Serializable {
  def exists(msg: Seq[Array[Byte]]): Seq[Boolean]

  def close(): Unit
}
