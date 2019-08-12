package stream.filter.kafka.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.io.IOException;

public class JsonToEntityMapper implements ValueMapper<String, Entity> {
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public Entity apply(String value) {
        try {
            return mapper.readValue(value, Entity.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
