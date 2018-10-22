package json;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * Will count appearance of each key for all time
 */
public class KafkaCountByKeyWithoutWindowing {
    private static final Logger logger = Logger.getLogger(KafkaCountByKeyWithoutWindowing.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka.json.count");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> source = builder.stream("kafka.json");
        source
                .peek((k, v) -> logger.info(String.format("[INPUT][%s]: %s", k, v)))
                .groupByKey()
                .count()
                .toStream()
                .peek((k, v) -> logger.info(String.format("[RESULT][%s]: %d", k, v)));
        Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, properties);
        streams.start();
    }
}
