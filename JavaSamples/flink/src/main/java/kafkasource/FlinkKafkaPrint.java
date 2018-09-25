package kafkasource;


import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Properties;

/**
 * Execute `select floor(random()*5)+1 as key, (ARRAY['SET', 'UNSET', 'NONE'])[floor(random()*3)+1] as action, md5(random()::text) as value`
 * from nifi ExecuteSQL processor and put result into nifi.from topic.
 *
 * Then read messages and just print
 */
public class FlinkKafkaPrint {
    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "flink-sample");
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        String topic = "nifi.from";

        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<ObjectNode> source = executionEnvironment.addSource(new FlinkKafkaConsumer011<>(topic, new JSONKeyValueDeserializationSchema(false), properties));
        source.print();

        executionEnvironment.execute();
    }
}
