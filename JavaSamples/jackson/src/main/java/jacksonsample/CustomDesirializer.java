package jacksonsample;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class CustomDesirializer extends JsonDeserializer<Entity> {
    public Entity deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        Entity entity = new Entity();

        JsonNode tree = jsonParser.getCodec().readTree(jsonParser);

        entity.setField_one(tree.get("custom_field_one").asText());
        entity.setField_two(tree.get("custom_field_two").asText());
        entity.setField_three(tree.get("custom_field_three").asText());

        return entity;
    }
}
