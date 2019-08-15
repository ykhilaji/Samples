package stream.filter.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class DynamicRouteProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Result result = (Result) exchange.getIn().getBody();

        exchange.getIn().setHeader("IS_EXIST", result.exist);
        exchange.getIn().setBody(result.entity);
    }
}
