package stream.filter.akka.streams

import akka.actor.ActorSystem
import akka.{Done, NotUsed}
import akka.kafka.{CommitterSettings, ConsumerMessage, ProducerMessage, ProducerSettings}
import akka.kafka.scaladsl.{Committer, Producer}
import akka.stream.scaladsl.{Flow, Sink}
import com.typesafe.config.Config
import org.apache.kafka.clients.producer.{ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future

object Sink {
  // Not used because this implementation commits messages one by one -> slow
  def kafkaSink(config: Config): Sink[ProducerMessage.Envelope[String, String, ConsumerMessage.Committable], Future[Done]] = {
    val producerSettings =
      ProducerSettings(config, new StringSerializer, new StringSerializer)
        .withProperty(ProducerConfig.CLIENT_ID_CONFIG, "stream-filter-producer")
        .withProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "")

    Producer.committableSink(producerSettings)
  }

  // Allows to commit messages in batch (see commit method)
  def kafkaSinkPlain(config: Config): Flow[ProducerMessage.Envelope[String, String, ConsumerMessage.Committable], ProducerMessage.Results[String, String, ConsumerMessage.Committable], NotUsed] = {
    val targetConfig = config.getConfig("filter.kafka.target")

    val producerSettings =
      ProducerSettings(config.getConfig("akka.kafka.producer"), new StringSerializer, new StringSerializer)
        .withProperty(ProducerConfig.CLIENT_ID_CONFIG, "stream-filter-producer")
        .withProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, targetConfig.getString("servers"))

    Producer.flexiFlow(producerSettings)
  }

  def commit(implicit system: ActorSystem): Sink[ConsumerMessage.Committable, Future[Done]] = {
    val settings = CommitterSettings(system)

    Committer.sink(settings.withMaxBatch(1000).withParallelism(4))
  }

  def toProducerRecord(config: Config) = {
    val targetConfig = config.getConfig("filter.kafka.target")

    Flow[CommitableMessage[String, String]].map { msg =>
      ProducerMessage.single(
        new ProducerRecord(targetConfig.getString("topic"), msg.key, msg.value),
        msg.committableOffset
      )
    }
  }
}
