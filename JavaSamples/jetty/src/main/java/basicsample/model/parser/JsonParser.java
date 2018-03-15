package basicsample.model.parser;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParser {
    public static ObjectMapper mapper = null;

    static {
        mapper = new ObjectMapper();
    }
}
