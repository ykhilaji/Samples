package json;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.TimeWindowedKStream;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.log4j.Logger;

import java.time.Duration;
import java.util.Properties;

/**
 * Will count appearance of each key for fixed time window
 */
public class KafkaFixedWindowCountByKey {
    static final Logger logger = Logger.getLogger(KafkaFixedWindowCountByKey.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka.json.windowcount");
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        // Fixed commit interval to see results of 10-seconds window each 10 seconds
        properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024L); // 10 mb
        properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, Duration.ofSeconds(10).toMillis());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> source = builder.stream("kafka.json");
        source = source.peek((k, v) -> logger.info(String.format("[INPUT][%s]: %s", k, v.toString())));
        // Will create topic for storing state changelog for fault-tolerance (~kafka.json.windowcount-KSTREAM-AGGREGATE-STATE-STORE-0000000002-changelog)
        TimeWindowedKStream<String, JsonNode> windowed = source.groupByKey().windowedBy(TimeWindows.of(Duration.ofSeconds(10).toMillis()));
        windowed.count().toStream().peek((k, v) -> logger.info(String.format("[RESULT][%s]: Count: %d", k.key(), v)));
        Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, properties);
        streams.start();
    }
}
