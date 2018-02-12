package jacksonannotationsample;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class CustomDesirializer extends JsonDeserializer<Entity> {
    public Entity deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        Entity entity = new Entity();

        JsonNode tree = jsonParser.getCodec().readTree(jsonParser);

        entity.setId(tree.get("id").asInt());
        entity.setField(tree.get("field").asText());
        entity.setValue(tree.get("value").asText());

        return entity;
    }
}
