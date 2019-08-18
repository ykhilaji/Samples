package stream.filter.flink

import org.apache.flink.streaming.api.scala.createTypeInformation

object TypeInfo {
  implicit val messageTypeInfo = createTypeInformation[model.Message]
  implicit val messageSeqTypeInfo = createTypeInformation[Seq[model.Message]]
}
