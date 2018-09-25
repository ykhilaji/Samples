package kafkasource;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * Execute `select floor(random()*5)+1 as key, (ARRAY['SET', 'UNSET', 'NONE'])[floor(random()*3)+1] as action, md5(random()::text) as value`
 * from nifi ExecuteSQL processor and put result into nifi.from topic
 * <p>
 * Then read messages and filter messages with `NONE` as action value
 */
public class FlinkKafkaFilter {
    private static Logger logger = Logger.getLogger(FlinkKafkaFilter.class.getName());

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "filter-none-actions");

        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();

        /**
         * JSONKeyValueDeserializationSchema - values come in format {"key": ?, "value": ?}
         * If key does not exist, then key field skipped
         */
        DataStream<ObjectNode> source = executionEnvironment.addSource(new FlinkKafkaConsumer011<>("nifi.from", new JSONKeyValueDeserializationSchema(false), properties));
        source = source.process(new ProcessFunction<ObjectNode, ObjectNode>() {
            @Override
            public void processElement(ObjectNode value, Context ctx, Collector<ObjectNode> out) throws Exception {
                logger.info(String.format("[INPUT]: %s", value));
                out.collect(value);
            }
        }).forward();
        DataStream<ObjectNode> filtered = source.filter((FilterFunction<ObjectNode>) jsonNodes -> !"NONE".equals(jsonNodes.get("value").get("action").asText()));
        filtered.process(new ProcessFunction<ObjectNode, ObjectNode>() {
            @Override
            public void processElement(ObjectNode value, Context ctx, Collector<ObjectNode> out) throws Exception {
                logger.info(String.format("[FILTERED]: %s", value));
                out.collect(value);
            }
        });

        executionEnvironment.execute();
    }
}
