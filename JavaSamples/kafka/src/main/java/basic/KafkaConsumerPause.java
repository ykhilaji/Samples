package basic;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KafkaConsumerPause {
    public static void main(String[] args) {
        KafkaConsumer kafkaConsumer = new KafkaConsumer(new Properties());
        kafkaConsumer.subscribe(Collections.singletonList("topic"));

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        while (true) {
            ConsumerRecords<Object, Object> records = kafkaConsumer.poll(100);

            final CountDownLatch countDownLatch = new CountDownLatch(records.count());

            for (ConsumerRecord record : records.records("topic")) {
                executorService.submit(() -> {
                    try {
                        // process record
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }

            if (countDownLatch.getCount() != 0) {
                kafkaConsumer.pause(records.partitions()); // to avoid rebalance
                // if broker will not get any messages from consumer it will think
                // that consumer is offline and cause rebalance partitions to other consumers in group
            }

            while (countDownLatch.getCount() != 0) {
                kafkaConsumer.poll(100);
            }

            kafkaConsumer.resume(records.partitions());
        }
    }
}
