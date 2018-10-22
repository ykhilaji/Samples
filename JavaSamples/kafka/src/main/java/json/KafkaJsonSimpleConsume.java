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
 * Consume JSON messages from kafka arbitrary topic. Messages are written into topic by Apache Nifi
 * ExecuteSQL: select (floor(random()*10)+1)::INT as key, md5(random()::text) as value, extract(EPOCH from now())::BIGINT as event_time
 * kafka-topics --create --topic kafka.json --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --config retention.bytes=26214400 --config retention.ms=36000
 */
public class KafkaJsonSimpleConsume {
    static final Logger logger = Logger.getLogger(KafkaJsonSimpleConsume.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka.json.consumer");
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> source = builder.stream("kafka.json");
        source.peek((k, v) -> logger.info(String.format("[INPUT][%s]: %s", k, v.toString())));

        Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, properties);
        streams.start();
    }
}
