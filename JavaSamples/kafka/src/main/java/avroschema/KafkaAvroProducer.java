package avroschema;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.avro.reflect.ReflectData;


import java.util.Properties;

public class KafkaAvroProducer {
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "uniqueProducerId");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put("schema.registry.url", "http://localhost:8081");

        KafkaProducer<String, GenericRecord> producer = new KafkaProducer<>(properties);

        Schema schema = ReflectData.get().getSchema(User.class);
        GenericRecord record = new GenericData.Record(schema);

        record.put("firstName", "someFirstName");
        record.put("lastName", "someLastName");

        ProducerRecord<String, GenericRecord> producerRecord = new ProducerRecord<>("sample", record);
        producer.send(producerRecord);

        producer.flush();
        producer.close();
    }
}
