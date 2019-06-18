package core.component;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class CustomComponentUsageSample {
    public static void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();

        CustomComponent in = new CustomComponent();
        CustomComponent out = new CustomComponent();

        camelContext.addComponent("in", in);
        camelContext.addComponent("out", out);

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("in").to("out");
            }
        });

        camelContext.start();
    }
}
