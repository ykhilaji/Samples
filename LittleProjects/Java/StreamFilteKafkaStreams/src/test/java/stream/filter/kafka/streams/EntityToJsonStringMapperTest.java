package stream.filter.kafka.streams;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityToJsonStringMapperTest {
    EntityToJsonStringMapper mapper = new EntityToJsonStringMapper();

    @Test
    public void jsonToEntity() {
        Entity entity = Entity.apply("1");
        String json = "{\"key\":\"1\"}";
        assertEquals(mapper.apply(entity), json);
    }
}
