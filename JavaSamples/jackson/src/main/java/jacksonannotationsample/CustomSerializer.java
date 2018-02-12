package jacksonannotationsample;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class CustomSerializer extends JsonSerializer<Entity> {
    public void serialize(Entity entity, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeNumberField("id", entity.getId());
        jsonGenerator.writeStringField("field", entity.getField());
        jsonGenerator.writeStringField("value", entity.getValue());

        jsonGenerator.writeEndObject();
    }
}
