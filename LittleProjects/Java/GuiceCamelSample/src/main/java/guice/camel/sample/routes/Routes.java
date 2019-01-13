package guice.camel.sample.routes;

import com.google.inject.Inject;
import guice.camel.sample.components.BeanComponent;
import org.apache.camel.builder.RouteBuilder;

public class Routes extends RouteBuilder {
    @Inject
    BeanComponent component;

    @Override
    public void configure() throws Exception {
        from("timer://guice?delay=5s")
                .setBody(constant("some message"))
                .bean(component);
    }
}
