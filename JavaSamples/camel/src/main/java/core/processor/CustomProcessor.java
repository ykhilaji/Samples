package core.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class CustomProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String in = (String) exchange.getIn().getBody();

        System.out.println(String.format("Message: %s", in));
        exchange.getOut().setBody(String.format("Processed: %s", in));
    }
}
