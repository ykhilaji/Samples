package core.withreply.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
public class ReplyConsumer {
    private static final Logger logger = LoggerFactory.getLogger("consumer");

    @SendTo(value = "reply")
    @JmsListener(destination = "sampleQueue")
    public String receive(String message) {
        logger.info(String.format("Message: %s", message));

        return "Reply from consumer";
    }
}
