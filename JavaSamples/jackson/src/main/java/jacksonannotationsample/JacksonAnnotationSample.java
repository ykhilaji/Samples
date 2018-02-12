package jacksonannotationsample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonAnnotationSample {
    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Entity entity = new Entity(1, "field", "value");

        System.out.println(mapper.writeValueAsString(entity)); // {"field":field <-- no quotes,"annotatedId":1}
    }
}
