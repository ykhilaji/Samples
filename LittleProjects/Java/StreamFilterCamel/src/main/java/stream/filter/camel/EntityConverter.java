package stream.filter.camel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;

import java.io.IOException;

public class EntityConverter implements TypeConverters {
    private final static ObjectMapper mapper = new ObjectMapper();

    @Converter
    public Entity fromJson(String body) throws IOException {
        return mapper.readValue(body, Entity.class);
    }

    @Converter
    public String toJson(Entity entity) throws JsonProcessingException {
        return mapper.writeValueAsString(entity);
    }

    @Converter
    public Entity fromBytes(byte[] bytes) throws IOException {
        return mapper.readValue(bytes, Entity.class);
    }
}
