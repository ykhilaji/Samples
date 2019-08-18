package stream.filter.flink

import model.Message
import org.apache.thrift.protocol.TCompactProtocol
import org.apache.thrift.transport.TMemoryBuffer

object MessageUtils {
  private val protocolFactory = new TCompactProtocol.Factory() // no string length limit
  private val LENGTH = 64

  def fromBytes(value: Array[Byte]): Message = {
    val buffer = new TMemoryBuffer(value.length)
    val msg = new model.Message()
    buffer.write(value)
    msg.read(protocolFactory.getProtocol(buffer))

    msg
  }

  def toBytes(msg: Message): Array[Byte] = {
    val buffer = new TMemoryBuffer(LENGTH)
    msg.write(protocolFactory.getProtocol(buffer))

    buffer.getArray
  }
}
