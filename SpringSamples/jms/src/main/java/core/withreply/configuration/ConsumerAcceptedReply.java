package core.withreply.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class ConsumerAcceptedReply {
    private static final Logger logger = LoggerFactory.getLogger("ConsumerAcceptedReply");

    @JmsListener(destination = "reply")
    public void receive(String message) {
        logger.info(String.format("Got reply: %s", message));
    }
}
