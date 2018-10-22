package json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.log4j.Logger;

import java.time.Duration;
import java.util.Properties;

/**
 * ExecuteSQL: select (floor(random()*10)+1)::INT as key, md5(random()::text) as value, extract(EPOCH from now())::BIGINT as event_time
 * ExecuteSQL: select (floor(random()*10)+1)::INT as key, (floor(random()*100)+1)::INT as value, extract(EPOCH from now())::BIGINT as event_time
 */
public class KafkaStreamStreamJoin {
    private static final Logger logger = Logger.getLogger(KafkaStreamStreamJoin.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka.json.join");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        // Use current wall clock time for advancing window
        properties.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class.getName());

        StreamsBuilder builder = new StreamsBuilder();

        final ObjectMapper mapper = new ObjectMapper();
        KStream<String, JsonNode> first = builder.stream("kafka.json");
        KStream<String, JsonNode> second = builder.stream("kafka.json.second");
        first = first.peek((k, v) -> logger.info(String.format("[FIRST][%s]: %s", k, v.toString())));
        second = second.peek((k, v) -> logger.info(String.format("[SECOND][%s]: %s", k, v.toString())));

        // will create 2 additional topics for store changelog of state:
        // kafka.json.join-KSTREAM-JOINTHIS-0000000006-store-changelog
        // kafka.json.join-KSTREAM-JOINOTHER-0000000007-store-changelog
        KStream<String, JsonNode> joined = first.join(second, (value1, value2) -> {
            ObjectNode node = mapper.createObjectNode();
            node.set("key", value1.get("key"));
            node.set("value1", value1.get("value"));
            node.set("value2", value2.get("value"));
            node.set("event_time", value1.get("event_time"));

            return node;

        }, JoinWindows.of(Duration.ofSeconds(10).toMillis()));
        joined.peek((k, v) -> logger.info(String.format("[JOINED][%s]: %s", k, v.toString())));

        Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, properties);
        streams.start();
    }
}
