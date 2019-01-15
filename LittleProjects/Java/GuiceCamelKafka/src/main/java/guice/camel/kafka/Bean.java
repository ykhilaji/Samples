package guice.camel.kafka;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

public class Bean {
    private static final Logger logger = LogManager.getLogger("bean");
    Endpoint endpoint;
    ProducerTemplate template;

    @Inject
    public Bean(@Named("to") KafkaComponent component, CamelContext context) throws Exception {
        this.endpoint = component.createEndpoint("kafka:from");
        this.template = context.createProducerTemplate();
    }

    public void send(Object body) {
        logger.info(String.format("Send: %s", body));

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(KafkaConstants.KEY, null);

        template.sendBodyAndHeaders(endpoint, body, headers);
    }
}
