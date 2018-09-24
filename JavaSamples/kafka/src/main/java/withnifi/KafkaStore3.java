package withnifi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KafkaStore3 {
    static final String TOPIC_FROM = "nifi.from";
    static final String BOOTSTRAP_SERVER = "localhost:9092";
    static final Logger logger = Logger.getLogger(KafkaStore3.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "store3");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Map<String, String> changelogConfig = new HashMap<>();

        StreamsBuilder builder = new StreamsBuilder();
        StoreBuilder storeBuilder = Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore("persistent-store"), // will create RockDB store
                Serdes.String(), Serdes.String()
        ).withCachingEnabled().withLoggingEnabled(changelogConfig);
        builder.addStateStore(storeBuilder);

        ObjectMapper mapper = new ObjectMapper();
        KStream<String, String> source = builder.stream(TOPIC_FROM);
        source
                .peek((k, v) -> logger.info(String.format("[INPUT][%s]: %s", k, v)))
                .transform(() -> new Transformer<String, String, KeyValue<String, String>>() {
                    private ProcessorContext context;
                    private KeyValueStore<String, String> stateStore;

                    @Override
                    public void init(ProcessorContext context) {
                        this.context = context;
                        this.stateStore = (KeyValueStore<String, String>) this.context.getStateStore("persistent-store");
                        this.context.schedule(10 * 1000, PunctuationType.WALL_CLOCK_TIME, this::punctuate); // punctuate each 10 seconds
                    }

                    @Override
                    public KeyValue<String, String> transform(String key, String value) {
                        logger.info(String.format("[TRANSFORM][%s]: %s", key, value));
                        try {
                            JsonNode node = mapper.readTree(value);
                            stateStore.put(node.get("key").asText(), value);
                            context.commit();

                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }

                        return null; // do not return any values. Use context.forward in punctuate (for batch updating)
                    }

                    @Override
                    public KeyValue<String, String> punctuate(long timestamp) {
                        logger.info(String.format("[PUNCTUATE] %d", timestamp));

                        stateStore.all().forEachRemaining((kv) -> context.forward(kv.key, kv.value));
                        stateStore.flush();

                        return null;
                    }

                    @Override
                    public void close() {

                    }
                }, "persistent-store")
                .foreach((k, v) -> logger.info(String.format("[RESULT][%s]: %s", k, v)));



        Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, properties);
        streams.cleanUp();
        streams.start();

    }
}
