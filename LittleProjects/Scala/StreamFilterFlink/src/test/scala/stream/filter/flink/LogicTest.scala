package stream.filter.flink

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.test.util.AbstractTestBase
import org.scalatest.{Matchers, WordSpecLike}
import org.apache.flink.streaming.api.scala._
import com.typesafe.config.{Config, ConfigFactory}
import model.Message
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.sink.SinkFunction

import scala.collection.mutable


class LogicTest extends AbstractTestBase with WordSpecLike with Matchers {
  "Logic" should {
    "filter messages" in {
      val config = ConfigFactory.load()
      val env = StreamExecutionEnvironment.createLocalEnvironment(1)
      env.setParallelism(1)
      env.registerTypeWithKryoSerializer(classOf[Config], classOf[ConfigKryo])
      env.getConfig.addDefaultKryoSerializer(classOf[Message], classOf[TBaseSerializer])
      env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)


      val elements = Iterator.range(0, 10)
          .map(i => new model.Message(i.toString))
          .map(MessageUtils.toBytes)
          .toList

      val stream: DataStream[Array[Byte]] = env
        .fromCollection(elements)
        .assignAscendingTimestamps(_ => System.currentTimeMillis())

      val filtered = Logic.filter(stream, FilterUsingCache((config: Config) => InMemoryCache(msg => msg.key.toInt % 2 == 0), config))
      filtered.addSink(new CollectSink)

      env.execute()

      assertResult(CollectSink.values)(List(
        new model.Message("0"),
        new model.Message("2"),
        new model.Message("4"),
        new model.Message("6"),
        new model.Message("8"),
      ))
    }
  }
}

class CollectSink extends SinkFunction[model.Message] {

  override def invoke(value: model.Message): Unit = {
    synchronized {
      CollectSink.values += value
    }
  }
}

object CollectSink {

  // must be static
  val values: mutable.ListBuffer[model.Message] = mutable.ListBuffer()
}