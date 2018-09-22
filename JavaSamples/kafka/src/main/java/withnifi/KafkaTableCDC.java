package withnifi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class KafkaTableCDC {
    static final String TOPIC_FROM = "nifi.from";
    static final String BOOTSTRAP_SERVER = "localhost:9092";
    static final Logger logger = Logger.getLogger(KafkaTableCDC.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "streamtable");
        // disable cache to log all table changes
        // otherwise it will cache updates and write into changelog only last result
        properties.setProperty(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        final ObjectMapper mapper = new ObjectMapper();

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> kStream = builder.stream(TOPIC_FROM);
        // cause repartition because of key changing
        KStream<String, String> mapped = kStream.map((key, value) -> {
            try {
                JsonNode node = mapper.readTree(value);

                return new KeyValue<>(node.get("key").asText(), value);
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage());
                throw new RuntimeException(e);
            }
        });


        KStream<String, String> peeked = mapped.peek((k, v) -> logger.info(String.format("[input] Key: %s, Value: %s", k, v)));
        // will create 2 topics: 1 for store the result with compaction delete policy
        // and 1 for changelog of all updates
        KTable<String, String> table = peeked
                .groupByKey()
                .aggregate(() -> null, (key, next, current) -> {
                    try {
                        JsonNode nextNode = mapper.readTree(next);

                        if ("SET".equals(nextNode.get("action").asText())) {
                            return next;
                        } else if ("UNSET".equals(nextNode.get("action").asText())) {
                            return null;
                        } else if (current != null) {
                            return next;
                        } else {
                            return null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });

        KStream<String, String> filtered = table.toStream();
        filtered.filter((k, v) -> v != null).foreach((k, v) -> logger.info(String.format("[filtered] Key: %s, Value: %s", k, v)));

        Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, properties);
        streams.start();
    }
}
