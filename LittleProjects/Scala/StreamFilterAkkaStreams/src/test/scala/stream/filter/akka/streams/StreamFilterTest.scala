package stream.filter.akka.streams

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.testkit.TestKit
import com.typesafe.config.ConfigFactory
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.scalatest.{Matchers, WordSpecLike}

import scala.collection.immutable
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}

class StreamFilterTest extends TestKit(ActorSystem("stream-filter", ConfigFactory.load().getConfig("akka")))
  with WordSpecLike
  with Matchers
  with EmbeddedKafka {

  val config = ConfigFactory.load()
  val sourceConfig = config.getConfig("filter.kafka.source")
  val targetConfig = config.getConfig("filter.kafka.target")

  implicit val dispatcher = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val cache = InMemoryCache(entity => entity.key.matches("[13579]"))

  "Using embedded kafka StreamFilter app" should {
    "Filter messages from source topic and send filtered messages in target topic" in {
      implicit val embeddedKafkaConfig = EmbeddedKafkaConfig(kafkaPort = 12345)

      withRunningKafka {
        (0 to 10).foreach(i => {
          publishStringMessageToKafka(sourceConfig.getString("topic"), JsonUtils.toJson(Entity(i.toString)))
        })

        createCustomTopic(targetConfig.getString("topic"))

        val stream = Source.kafkaSource(config)
          .via(Logic.toEntity())
          .via(Logic.filterBatchAsync(cache, 100, FiniteDuration(100, TimeUnit.MILLISECONDS), 4))
          .map(_.asInstanceOf[CommitableMessage[String, Entity]])
          .via(Logic.toJsonString())
          .via(Sink.toProducerRecord(config))
          .via(Sink.kafkaSinkPlain(config))
          .map(_.passThrough)
          .toMat(Sink.commit)(Keep.both)
          .mapMaterializedValue(DrainingControl.apply)
          .run()

        Thread.sleep(1000)
        Await.ready(stream.shutdown(), Duration(5, TimeUnit.SECONDS))

        val result: immutable.Seq[String] = consumeNumberStringMessagesFrom(targetConfig.getString("topic"), 5, true)
        val expected: Seq[String] = Seq(Entity("1"), Entity("3"), Entity("5"), Entity("7"), Entity("9")).map(JsonUtils.toJson)

        assertResult(expected)(result)
      }
    }
  }
}
