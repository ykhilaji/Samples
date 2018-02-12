package jacksonsample;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class CustomSerializer extends JsonSerializer<Entity> {
    public void serialize(Entity entity, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("custom_field_one", entity.getField_one());
        jsonGenerator.writeStringField("custom_field_two", entity.getField_two());
        jsonGenerator.writeStringField("custom_field_three", entity.getField_three());

        jsonGenerator.writeEndObject();
    }
}
