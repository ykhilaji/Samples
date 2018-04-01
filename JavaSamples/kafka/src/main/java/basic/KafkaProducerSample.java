package basic;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaProducerSample {
    private static final String TOPIC = "sample";
    private static final String BOOTSTRAP_SERVER = "localhost:9092";

    public static void main(String[] args) throws InterruptedException {
        Properties properties = new Properties();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaProducerSample");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<Long, String> kafkaProducer = new KafkaProducer<>(properties);

        try {
            for (long i = 0; i < 10; ++i) {
                ProducerRecord<Long, String> record = new ProducerRecord<>(TOPIC, i, String.format("Message #%d", i));
                RecordMetadata metadata = kafkaProducer.send(record).get();

                System.out.println(String.format("Metadata - partition: %d, offset: %d, timestamp %d, topic: %s",
                        metadata.partition(),
                        metadata.offset(),
                        metadata.timestamp(),
                        metadata.topic()
                ));
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            kafkaProducer.flush();
            kafkaProducer.close();
        }
    }
}
