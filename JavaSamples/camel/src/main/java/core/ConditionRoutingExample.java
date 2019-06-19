package core;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.Random;

public class ConditionRoutingExample {
    public static void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        final Random random = new Random();

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("timer:simple?period=500")
                        .setBody(constant("Test"))
                        .process(exchange -> exchange.getIn().setHeader("SOME_HEADER", random.nextInt(100)))
                        .choice()
                        .when(exchange -> exchange.getIn().getHeader("SOME_HEADER", Integer.class) < 30)
                        .process(exchange -> System.out.println("< 30"))
                        .to("stream:out")
                        .when(exchange -> exchange.getIn().getHeader("SOME_HEADER", Integer.class) < 60)
                        .process(exchange -> System.out.println("< 60"))
                        .to("stream:out")
                        .otherwise()
                        .process(exchange -> System.out.println("Otherwise"))
                        .to("stream:out").stop()
                        .end()
                        .process(exchange -> System.out.println("After choice"))
                        .to("stream:out");
            }
        });

        camelContext.start();
        Thread.sleep(10000);
        camelContext.stop();
    }
}
