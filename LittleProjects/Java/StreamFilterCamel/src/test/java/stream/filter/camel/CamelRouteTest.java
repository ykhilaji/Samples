package stream.filter.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.TypeConversionException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.util.stream.Collectors;

public class CamelRouteTest extends CamelTestSupport {
    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                onException(TypeConversionException.class)
                        .handled(true)
                        .log("Incorrect json body: ${body}")
                        .to("mock:failover");

                from("direct:in")
                        .log("Input: ${body}")
                        .convertBodyTo(Entity.class)
                        .aggregate(constant(true), new ListAggregationStrategy())
                        .completionSize(3)
                        .completionTimeout(1000)
                        .log("Batch: ${body}")
                        .process(new InMemoryBatchFilterProcessor( e -> e.stream().map(n -> n.getKey().matches("^\\d+$")).collect(Collectors.toList())))
                        .log("Result: ${body}")
                        .split()
                        .body()
                        .convertBodyTo(String.class)
                        .log("Filtered: ${body}")
                        .to("mock:out");
            }
        };
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();
        context.getTypeConverterRegistry().addTypeConverters(new EntityConverter());
        return context;
    }

    @Test
    public void batchPipeline() throws InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:out");
        mock.expectedBodiesReceived("{\"key\":\"1\"}", "{\"key\":\"2\"}");

        template.sendBody("direct:in", "{\"key\":\"1\"}");
        template.sendBody("direct:in", "{\"key\":\"skip\"}");
        template.sendBody("direct:in", "{\"key\":\"2\"}");

        assertMockEndpointsSatisfied();
    }

    @Test
    public void errorHandling() throws InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:failover");
        mock.expectedBodiesReceived("incorrect json");

        template.sendBody("direct:in", "incorrect json");

        assertMockEndpointsSatisfied();
    }
}
