package stream.filter.akka.streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.{CommitterSettings, ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.stream.scaladsl.Flow
import com.typesafe.config.Config
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

object Source {
  def kafkaSource(config: Config) = {
    val sourceConfig = config.getConfig("filter.kafka.source")

    val consumerSettings =
      ConsumerSettings(config.getConfig("akka.kafka.consumer"), new StringDeserializer, new StringDeserializer)
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
        .withProperty(ConsumerConfig.CLIENT_ID_CONFIG, "stream-filter-consumer")
        .withProperty(ConsumerConfig.GROUP_ID_CONFIG, "stream-filter-consumers")
        .withProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, sourceConfig.getString("servers"))

    Consumer.committableSource(consumerSettings, Subscriptions.topics(sourceConfig.getString("topic")))
  }

  // Not used because commit will be on producer side
  def commit(implicit system: ActorSystem): Flow[ConsumerMessage.Committable, ConsumerMessage.CommittableOffsetBatch, NotUsed] = {
    val settings = CommitterSettings(system)

    Committer.batchFlow(settings.withMaxBatch(1000).withParallelism(4))
  }
}
