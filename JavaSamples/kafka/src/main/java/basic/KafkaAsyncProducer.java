package basic;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaAsyncProducer {
    private static final String TOPIC = "sample";
    private static final String BOOTSTRAP_SERVER = "localhost:9092";

    public static void main(String[] args) throws InterruptedException {
        Properties properties = new Properties();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaProducerSample");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // properties.put(ProducerConfig.ACKS_CONFIG, "0"); // will not wait for reply from broker
         properties.put(ProducerConfig.ACKS_CONFIG, "1"); // will wait for reply from 1 broker
        // properties.put(ProducerConfig.ACKS_CONFIG, "all"); // will wait for reply from all brokers

        KafkaProducer<Long, String> kafkaProducer = new KafkaProducer<>(properties);

        for (long i = 0; i < 10; ++i) {
            ProducerRecord<Long, String> record = new ProducerRecord<>(TOPIC, i, String.format("Message #%d", i));
            kafkaProducer.send(record, (recordMetadata, e) -> {
                if (e != null) {
                    e.printStackTrace();
                } else {
                    System.out.println(recordMetadata.offset());
                }
            });
        }
        kafkaProducer.flush();
        kafkaProducer.close();
    }
}
