package basic;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;

public class KafkaConsumerSeekSpecificOffset {
    private KafkaConsumer kafkaConsumer;
    private long specificOffset = 10;

    public class RebalanceListener implements ConsumerRebalanceListener {
        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            // commit
        }

        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            for (TopicPartition partition : partitions) {
                kafkaConsumer.seek(partition, specificOffset);
            }
        }
    }
}
