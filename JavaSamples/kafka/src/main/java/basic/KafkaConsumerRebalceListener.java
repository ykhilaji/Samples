package basic;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.*;

public class KafkaConsumerRebalceListener {
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaConsumerSample");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9042");

        Map<TopicPartition, OffsetAndMetadata> partitionOffsetAndMetadataMap = new HashMap<>();

        KafkaConsumer<Long, String> kafkaConsumer = new KafkaConsumer<>(properties);

        kafkaConsumer.subscribe(Collections.singletonList("sample"), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                System.out.println("Partitions were revoked");
                kafkaConsumer.commitSync(partitionOffsetAndMetadataMap);

            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                System.out.println("Assigned partitions: " + partitions.toString());
            }
        });


        int totalRecords = 0;

        while (totalRecords < 10) {
            final ConsumerRecords<Long, String> consumerRecords = kafkaConsumer.poll(1000);

            for (TopicPartition partition : consumerRecords.partitions()) {
                for (ConsumerRecord<Long, String> record : consumerRecords.records(partition)) {
                    partitionOffsetAndMetadataMap.put(partition, new OffsetAndMetadata(record.offset() + 1));

                    totalRecords++;
                }
            }

            kafkaConsumer.commitAsync();
        }
        kafkaConsumer.close();
        System.out.println("DONE");
    }
}
