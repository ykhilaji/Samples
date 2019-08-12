package stream.filter.kafka.streams;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonToEntityMapperTest {
    JsonToEntityMapper mapper = new JsonToEntityMapper();

    @Test
    public void jsonToEntity() {
        String json = "{\"key\":\"1\"}";
        assertEquals(mapper.apply(json), Entity.apply("1"));
    }

    @Test
    public void throwError() {
        assertThrows(IllegalArgumentException.class, () -> mapper.apply(""));
    }
}
