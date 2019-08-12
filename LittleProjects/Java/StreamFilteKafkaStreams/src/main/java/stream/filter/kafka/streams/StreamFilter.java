package stream.filter.kafka.streams;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class StreamFilter {
    private static final Logger logger = LoggerFactory.getLogger(StreamFilter.class);

    public static void main(String[] args) {
        Config config = ConfigFactory.load().getConfig("filter");

        Properties properties = properties(config);
        Topology topology = topology(new RedisCacheBuilder(), config);
        KafkaStreams streams = new KafkaStreams(topology, properties);

        streams.setUncaughtExceptionHandler((t, e) -> logger.error("Uncaught error", e));
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

        streams.start();
    }

    public static Topology topology(CacheBuilder cacheBuilder, Config config) {
        Config kafkaConfig = config.getConfig("kafka");
        Config cacheConfig = config.getConfig("cache");

        StreamsBuilder builder = new StreamsBuilder();

        builder
                .stream(kafkaConfig.getString("source.topic"), Consumed.with(new Serdes.StringSerde(), new Serdes.StringSerde()))
                .mapValues(new JsonToEntityMapper())
                .transform(new FilterLogicSupplier(cacheBuilder, cacheConfig))
                .mapValues(new EntityToJsonStringMapper())
                .to(kafkaConfig.getString("target.topic"));

        return builder.build();
    }

    public static Properties properties(Config config) {
        Config kafkaConfig = config.getConfig("kafka");

        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getString("brokers"));
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "stream-filter");
        properties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 4);
        properties.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);
        properties.put(StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG, ProcessingExceptionHandler.class);

        return properties;
    }
}
