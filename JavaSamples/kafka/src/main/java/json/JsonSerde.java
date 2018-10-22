package json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.util.Map;

public class JsonSerde implements Serde<JsonNode> {
    private JsonSerializer serializer = new JsonSerializer();
    private JsonDeserializer deserializer = new JsonDeserializer();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }

    @Override
    public Serializer<JsonNode> serializer() {
        return serializer;
    }

    @Override
    public Deserializer<JsonNode> deserializer() {
        return deserializer;
    }

    private static class JsonSerializer implements Serializer<JsonNode> {
        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {

        }

        @Override
        public byte[] serialize(String topic, JsonNode data) {
            try {
                return data.binaryValue();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() {

        }
    }

    private static class JsonDeserializer implements Deserializer<JsonNode> {
        private ObjectMapper mapper = new ObjectMapper();

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {

        }

        @Override
        public JsonNode deserialize(String topic, byte[] data) {
            try {
                if (data != null) {
                    return mapper.readTree(data);
                } else {
                    return mapper.createObjectNode();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() {

        }
    }
}
