package basic;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerSample {
    private static final String TOPIC = "sample";
    private static final String BOOTSTRAP_SERVER = "localhost:9092";

    public static void main(String[] args) {
        Properties properties = new Properties();

        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaConsumerSample");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        properties.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 200);
        properties.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000");

        KafkaConsumer<Long, String> kafkaConsumer = new KafkaConsumer<>(properties);
        kafkaConsumer.subscribe(Collections.singletonList(TOPIC));


        int totalRecords = 0;

        while (totalRecords < 10) {
            final ConsumerRecords<Long, String> consumerRecords = kafkaConsumer.poll(1000);

            for (ConsumerRecord<Long, String> record : consumerRecords.records(TOPIC)) {
                System.out.printf("Consumer Record:(%d, %s, %d, %d)\n",
                        record.key(),
                        record.value(),
                        record.partition(),
                        record.offset()
                );

                totalRecords++;
            }
        }
        kafkaConsumer.close();
        System.out.println("DONE");
    }
}
