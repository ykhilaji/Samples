package project

import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TMemoryBuffer
import org.scalatest.{Matchers, WordSpec}
import thriftSample.Message

class ThriftSerializationTest extends WordSpec with Matchers {
  "thrift serialization" should {
    "serialize and deserialize correctly" in {
      val message = Message(1, "body")

      val protocolFactory: TBinaryProtocol.Factory = new TBinaryProtocol.Factory()
      val buffer = new TMemoryBuffer(64)
      val protocol = protocolFactory.getProtocol(buffer)

      Message.encode(message, protocol)

      val decoded = Message.decode(protocol)

      assertResult(message)(decoded)
    }
  }
}
