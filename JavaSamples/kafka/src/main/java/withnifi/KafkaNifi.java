package withnifi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KafkaNifi {
    static final String TOPIC_FROM = "nifi.from";
    static final String TOPIC_TO = "nifi.to";
    static final String BOOTSTRAP_SERVER = "localhost:9092";
    static final String CONSUMER_GROUP_ID = "from";
    static final String PRODUCER_GROUP_ID = "to";
    static final Logger logger= Logger.getLogger(KafkaNifi.class);

    public static void main(String[] args) {
        Properties consumerProperties = new Properties();
        consumerProperties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        consumerProperties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
        consumerProperties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        Properties producerProperties = new Properties();
        producerProperties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        producerProperties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.setProperty(ProducerConfig.CLIENT_ID_CONFIG, PRODUCER_GROUP_ID);
        producerProperties.setProperty(ProducerConfig.ACKS_CONFIG, "all");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties);
        consumer.subscribe(Collections.singletonList(TOPIC_FROM));

        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProperties);

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        final Pattern pattern = Pattern.compile("(.).+(.)");
        final ObjectMapper mapper = new ObjectMapper();


        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(500);

                for (ConsumerRecord<String, String> record : records) {
                    logger.info(String.format("Key: %s Value: %s", record.key(), record.value()));

                    JsonNode node = mapper.readTree(record.value());
                    String value = node.get("value").asText();
                    Matcher matcher = pattern.matcher(value);

                    if (matcher.find() &&
                            matcher.groupCount() > 1 &&
                            (Character.isDigit(matcher.group(1).charAt(0)) && Character.isDigit(matcher.group(2).charAt(0)) ||
                            Character.isLetter(matcher.group(1).charAt(0)) && Character.isLetter(matcher.group(2).charAt(0)))) {
                        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC_TO, record.key(), value);
                        producer.send(producerRecord, (metadata, exception) -> {
                            if (exception != null) {
                                logger.error(exception.getLocalizedMessage());
                            } else {
                                logger.info("Message was successfully delivered");
                            }
                        });
                    } else {
                        logger.info("Skip message");
                    }
                }

                consumer.commitSync();
            }
        } catch (WakeupException w) {
            // pass
        } catch (Exception e) {
            consumer.commitSync();
            e.printStackTrace();
        } finally {
            consumer.close();
        }

    }
}
