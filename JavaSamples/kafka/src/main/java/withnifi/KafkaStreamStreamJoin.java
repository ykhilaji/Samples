package withnifi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

public class KafkaStreamStreamJoin {
    static final String TOPIC_FROM = "nifi.from";
    static final String BOOTSTRAP_SERVER = "localhost:9092";
    static final Logger logger= Logger.getLogger(KafkaStreamStreamJoin.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "streamstreamjoin");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        final ObjectMapper mapper = new ObjectMapper();

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> kStream = builder.stream(TOPIC_FROM);
        KStream<String, String> mapped = kStream.mapValues((v) -> {
            JsonNode node = null;
            try {
                node = mapper.readTree(v);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return node.get("value").asText();
        });
        KStream<String, String> upperStream = mapped.mapValues((k, v) -> v.toUpperCase());

        final Pattern startsWithDigitPattern = Pattern.compile("^\\d");
        final Pattern startsWithCharacterPattern = Pattern.compile("^[a-zA-Z]");
        Predicate<String, String> startsWithDigitPredicate = (k, v) -> startsWithDigitPattern.matcher(v).find();
        Predicate<String, String> startsWithCharacterPredicate = (k, v) -> startsWithCharacterPattern.matcher(v).find();

        int startsWithDigit = 0;
        int startsWithCharacter = 1;

        KStream<String, String>[] branches = upperStream.branch(startsWithDigitPredicate, startsWithCharacterPredicate);

        KStream<String, String> startsWithDigitStream = branches[startsWithDigit];
        KStream<String, String> startsWithCharacterStream = branches[startsWithCharacter];

        // forEach is terminal operation
        // use peek if you need to continue processing
        startsWithDigitStream.foreach((k, v) -> logger.info(String.format("Starts with digit value: %s", v)));
        startsWithCharacterStream.foreach((k, v) -> logger.info(String.format("Starts with character value: %s", v)));

        final AtomicLong digit = new AtomicLong(0);
        final AtomicLong characters = new AtomicLong(0);

        // cause repartition if after selectKey is going aggregate, join, groupBy
        KStream<String, String> startsWithDigitStreamKey = startsWithDigitStream.selectKey((k, v) -> String.valueOf(digit.incrementAndGet()));
        KStream<String, String> startsWithCharacterStreamKey = startsWithCharacterStream.selectKey((k, v) -> String.valueOf(characters.incrementAndGet()));

        ValueJoiner<String, String, String> joiner = (s, s2) -> s.concat("_").concat(s2);

        final long MINUTE = 60 * 1000;
        JoinWindows windows = JoinWindows.of(MINUTE);

        KStream<String, String> joined = startsWithDigitStreamKey.join(startsWithCharacterStreamKey, joiner, windows);
        joined.foreach((k, v) -> logger.info(String.format("Joined result: [%s] %s", k, v)));

        Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, properties);
        streams.start();
    }
}
