package psqllistennotify;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class PSQLListenNotifySample {
    public static void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("pgevent:192.168.99.100:5432/postgres/events")
                        .to("stream:out");
            }
        });

        camelContext.start();

        Thread.sleep(10000);

        camelContext.stop();
    }
}
