package core.direct.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer {
    private static final Logger logger = LoggerFactory.getLogger("consumer");

    @JmsListener(destination = "sampleQueue")
    public void receive(String message) {
        logger.info(String.format("Message: %s", message));
    }
}
