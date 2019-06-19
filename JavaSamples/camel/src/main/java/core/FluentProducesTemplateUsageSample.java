package core;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class FluentProducesTemplateUsageSample {
    static class CustomProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            exchange.getIn().setBody(String.format("Processed message: %s", exchange.getIn().getBody()));
        }
    }

    public static void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").process(new CustomProcessor());
            }
        });

        camelContext.start();

        FluentProducerTemplate fluentProducerTemplate = camelContext.createFluentProducerTemplate();
        Object result = fluentProducerTemplate.clearAll().to("direct:start")
                .withBody("Test")
                .request();

        System.out.println(String.format("Result: %s", result));

        camelContext.stop();
    }
}
