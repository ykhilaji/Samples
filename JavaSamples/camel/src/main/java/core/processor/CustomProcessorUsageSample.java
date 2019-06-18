package core.processor;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class CustomProcessorUsageSample {
    public static void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("timer:simple?period=1000")
                        .setBody()
                        .simple("${id}")
                        .process(new CustomProcessor())
                        .to("stream:out");
            }
        });

        camelContext.start();
        Thread.sleep(5000);
        camelContext.stop();
    }
}
