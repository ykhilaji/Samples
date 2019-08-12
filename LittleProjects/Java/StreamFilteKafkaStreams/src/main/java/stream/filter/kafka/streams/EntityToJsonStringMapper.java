package stream.filter.kafka.streams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.streams.kstream.ValueMapper;


public class EntityToJsonStringMapper implements ValueMapper<Entity, String> {
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public String apply(Entity value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
