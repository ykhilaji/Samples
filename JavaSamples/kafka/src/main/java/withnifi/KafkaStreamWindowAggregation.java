package withnifi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class KafkaStreamWindowAggregation {
    static final String TOPIC_FROM = "nifi.from";
    static final String BOOTSTRAP_SERVER = "localhost:9092";
    static final Logger logger = Logger.getLogger(KafkaStreamWindowAggregation.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "streamwindowaggregation");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        StreamsBuilder builder = new StreamsBuilder();
        ObjectMapper mapper = new ObjectMapper();

        KStream<String, String> source = builder.stream(TOPIC_FROM);
        KStream<String, String> withKey = source
                .peek((k, v) -> logger.info(String.format("[input]: %s", v)))
                .selectKey((k, v) -> {
                    if (v != null) {
                        try {
                            JsonNode node = mapper.readTree(v);
                            return node.get("key").asText();
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }

                    return null;
                });

        // count total updates for each key for 10 seconds
        KGroupedStream<String, String> groupedStream = withKey.groupByKey();
        TimeWindowedKStream<String, String> windowedKStream = groupedStream.windowedBy(TimeWindows.of(1000 * 10));
        KTable<Windowed<String>, Long> aggregated = windowedKStream.aggregate(() -> 0L, (key, next, current) -> {
            try {
                JsonNode node = mapper.readTree(next);

                if ("SET".equals(node.get("action").asText())) {
                    return 1L + current;
                }

                return current;
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }, Materialized.<String, Long, WindowStore<Bytes, byte[]>>as("aggregation-result")
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.Long()));

        aggregated.toStream().foreach((k, v) -> logger.info(String.format("[%s]: %d", k, v)));

        Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, properties);

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        streams.start();

    }
}
