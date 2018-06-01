package basic;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collections;
import java.util.Properties;

public class KafkaCommitEachMessage {
    public static void main(String[] args) {
        Properties properties = new Properties();

        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaConsumerSample");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        KafkaConsumer<Long, String> kafkaConsumer = new KafkaConsumer<>(properties);
        kafkaConsumer.subscribe(Collections.singletonList("sample"));


        int totalRecords = 0;

        try {
            while (totalRecords < 10) {
                final ConsumerRecords<Long, String> consumerRecords = kafkaConsumer.poll(1000);

                for (TopicPartition partition : consumerRecords.partitions()) {
                    for (ConsumerRecord<Long, String> record : consumerRecords.records(partition)) {
                        System.out.printf("Consumer Record:(%d, %s, %d, %d)\n",
                                record.key(),
                                record.value(),
                                record.partition(),
                                record.offset()
                        );

                        kafkaConsumer.commitAsync(Collections.singletonMap(partition, new OffsetAndMetadata(record.offset() + 1)),
                                ((offsets, exception) -> {
                                    if (exception != null) {
                                        exception.printStackTrace();
                                    }
                                }));

                        totalRecords++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                kafkaConsumer.commitSync(); // important
            } finally {
                kafkaConsumer.close();
                System.out.println("DONE");
            }
        }
    }
}
