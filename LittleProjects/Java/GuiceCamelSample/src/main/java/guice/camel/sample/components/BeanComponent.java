package guice.camel.sample.components;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BeanComponent {
    private static final Logger logger = LogManager.getLogger("bean-component");

    public String consume(String message) {
        logger.info(String.format("Got message: %s", message));
        return message;
    }
}
