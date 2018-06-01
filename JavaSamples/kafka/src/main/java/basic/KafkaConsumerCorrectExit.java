package basic;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.util.Properties;

public class KafkaConsumerCorrectExit {
    public static void main(String[] args) {
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer(new Properties());

        Thread main = Thread.currentThread();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                kafkaConsumer.wakeup(); // raise WakeUp exception

                try {
                    main.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            while (true) {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
                for (ConsumerRecord<String, String> record : records.records("topic")) {
                    // process
                }
                kafkaConsumer.commitAsync();
            }
        } catch (WakeupException e) {
            // exit
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                kafkaConsumer.commitSync();
            } finally {
                kafkaConsumer.close();
            }
        }
    }
}
