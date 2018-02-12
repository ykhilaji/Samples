package jacksonsample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JacksonSample {
    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Entity entity = new Entity("field1", "field2", "field3");

        String json = mapper.writeValueAsString(entity);
        System.out.println(json); // {"field_one":"field1","field_two":"field2","field_three":"field3"}

        // custom serializer\desirializer
        SimpleModule module = new SimpleModule("custom");
        module.addSerializer(Entity.class, new CustomSerializer());
        module.addDeserializer(Entity.class, new CustomDesirializer());
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.registerModule(module);

        json = customMapper.writeValueAsString(entity);
        System.out.println(json); // {"custom_field_one":"field1","custom_field_two":"field2","custom_field_three":"field3"}
    }
}
