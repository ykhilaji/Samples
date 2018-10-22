package json;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * Separate single stream into 2 using predicates
 */
public class KafkaBranchStream {
    static final Logger logger = Logger.getLogger(KafkaBranchStream.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "kafka.json.branch");
        properties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> source = builder.stream("kafka.json");
        KStream<Integer, JsonNode> mapped = source.map((key, value) -> new KeyValue<>(Integer.parseInt(key), value));
        Predicate<Integer, JsonNode> oddKey = (key, value) -> key % 2 != 0;
        Predicate<Integer, JsonNode> evenKey = (key, value) -> key % 2 == 0;
        KStream<Integer, JsonNode>[] branched = mapped.branch(oddKey, evenKey);
        branched[0].peek((k, v) -> logger.info(String.format("[ODD][%s]: %s", k, v)));
        branched[1].peek((k, v) -> logger.info(String.format("[EVEN][%s]: %s", k, v)));

        Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, properties);
        streams.start();
    }
}
