package nifi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.api.common.functions.CoGroupFunction;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.IngestionTimeExtractor;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.ProcessingTimeTrigger;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.flink.streaming.connectors.nifi.NiFiDataPacket;
import org.apache.flink.streaming.connectors.nifi.NiFiSource;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.nifi.remote.client.SiteToSiteClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class FlinkNifiCoGroupSplittedJoin {
    static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        DataStream<NiFiDataPacket> left = env.addSource(new NiFiSource(new SiteToSiteClient.Builder()
                .url("http://localhost:8080/nifi")
                .portName("left")
                .requestBatchCount(10)
                .buildConfig()));
        DataStream<NiFiDataPacket> right = env.addSource(new NiFiSource(new SiteToSiteClient.Builder()
                .url("http://localhost:8080/nifi")
                .portName("right")
                .requestBatchCount(10)
                .buildConfig()));

        DataStream<JsonNode> leftJson = left.map(d -> toJson(d.getContent())).assignTimestampsAndWatermarks(new IngestionTimeExtractor<>());
        DataStream<JsonNode> rightJson = right.map(d -> toJson(d.getContent())).assignTimestampsAndWatermarks(new IngestionTimeExtractor<>());

        SplitStream<JsonNode> leftSplitted = leftJson.split(FlinkNifiCoGroupSplittedJoin::split);
        SplitStream<JsonNode> rightSplitted = rightJson.split(FlinkNifiCoGroupSplittedJoin::split);

        DataStream<JsonNode> leftOdd = leftSplitted.select("odd");
        DataStream<JsonNode> leftEven = leftSplitted.select("even");
        DataStream<JsonNode> rightOdd = rightSplitted.select("odd");
        DataStream<JsonNode> rightEven = rightSplitted.select("even");

        DataStream<JsonNode> coGroupped = leftOdd.coGroup(rightOdd)
                .where((KeySelector<JsonNode, JsonNode>) value -> value.get("id"))
                .equalTo((KeySelector<JsonNode, JsonNode>) value -> value.get("id"))
                .window(TumblingEventTimeWindows.of(Time.seconds(5)))
                .trigger(ProcessingTimeTrigger.create())
                .apply(new CoGroupFunction<JsonNode, JsonNode, JsonNode>() {
                    @Override
                    public void coGroup(Iterable<JsonNode> first, Iterable<JsonNode> second, Collector<JsonNode> out) throws Exception {
                        out.collect(coGroupJson(first, second));
                    }
                });

        DataStream<JsonNode> joined = leftEven.join(rightEven)
                .where((KeySelector<JsonNode, JsonNode>) value -> value.get("id")).equalTo((KeySelector<JsonNode, JsonNode>) value -> value.get("id"))
                .window(TumblingEventTimeWindows.of(Time.seconds(5)))
                .trigger(ProcessingTimeTrigger.create())
                .apply(new JoinFunction<JsonNode, JsonNode, JsonNode>() {
                    @Override
                    public JsonNode join(JsonNode first, JsonNode second) throws Exception {
                        return merge(first, second);
                    }
                });

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        properties.setProperty(ProducerConfig.CLIENT_ID_CONFIG, "nifiJoin");

        DataStream<JsonNode> result = coGroupped.union(joined);
        result.print();

        result.addSink(new FlinkKafkaProducer011<>("nifi", new KSS(), properties));

        env.execute();
    }

    static Iterable<String> split(JsonNode value) {
        long id = value.get("id").asLong();

        if (id % 2 == 0) {
            return Collections.singleton("even");
        }

        return Collections.singleton("odd");
    }

    static JsonNode toJson(byte[] data) throws IOException {
        return mapper.readTree(data);
    }

    static JsonNode merge(JsonNode left, JsonNode right) {
        ObjectNode node = mapper.createObjectNode();

        node.set("id", left.get("id"));
        node.set("left", left.get("value"));
        node.set("right", right.get("value"));

        return node;
    }

    static JsonNode coGroupJson(Iterable<JsonNode> left, Iterable<JsonNode> right) {
        ObjectNode node = mapper.createObjectNode();
        List<JsonNode> ll = new ArrayList<>();
        List<JsonNode> rl = new ArrayList<>();

        left.forEach(ll::add);
        right.forEach(rl::add);

        long id = 0;
        if (!ll.isEmpty()) {
            id = ll.get(0).get("id").asLong();
        } else if (!rl.isEmpty()) {
            id = rl.get(0).get("id").asLong();
        }

        node.put("id", id);
        node.putPOJO("left", ll);
        node.putPOJO("right", rl);

        return node;
    }

    static class KSS implements KeyedSerializationSchema<JsonNode> {
        @Override
        public byte[] serializeKey(JsonNode jsonNode) {
            try {
                return mapper.writeValueAsBytes(jsonNode.get("id"));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public byte[] serializeValue(JsonNode jsonNode) {
            try {
                return mapper.writeValueAsBytes(jsonNode);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getTargetTopic(JsonNode jsonNode) {
            return null;
        }
    }
}
