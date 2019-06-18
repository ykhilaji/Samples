package core.component;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;

public class CustomProducer extends DefaultProducer {
    CustomEndpoint endpoint;

    public CustomProducer(CustomEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    public void process(Exchange exchange) throws Exception {
        System.out.println(String.format("Processed message: %s", exchange.getMessage().getBody()));
    }
}
