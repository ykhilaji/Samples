package stream.filter.akka.streams

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.scaladsl.Keep
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.duration.FiniteDuration

object StreamFilter {
  private val logger = LoggerFactory.getLogger(StreamFilter.getClass)

  def main(args: Array[String]): Unit = {
    logger.info("Starting StreamFilter app")

    val config = ConfigFactory.load()

    val cacheConfig = config.getConfig("filter.cache")

    implicit val system = ActorSystem("stream-filter", config)
    implicit val dispatcher = system.dispatcher

    val decider: Supervision.Decider = {
      case _ => Supervision.Resume // just skip incorrect messages
    }
    implicit val materializer = ActorMaterializer(
      ActorMaterializerSettings(system).withSupervisionStrategy(decider)
    )

    val cache = AerospikeCache(config.getConfig("filter.cache"))

    Source.kafkaSource(config)
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

    logger.info("StreamFilter app was started")

    sys.addShutdownHook({
      logger.info("Shutdown actor system")
      system.terminate().onComplete({
        case scala.util.Success(_) => logger.info("Actor system was successfully terminated")
        case scala.util.Failure(exception) => logger.error("Actor system was terminated abnormally", exception)
      })
    })
  }
}
