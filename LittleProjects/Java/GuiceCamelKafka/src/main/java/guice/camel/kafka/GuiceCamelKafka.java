package guice.camel.kafka;

import com.google.inject.Guice;
import org.apache.camel.guice.CamelModuleWithRouteTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiceCamelKafka {
    public static void main(String[] args) throws InterruptedException {
        Logger logger = LogManager.getLogger("GuiceCamelKafka");
        logger.info("Start GuiceCamelKafka");
        Guice.createInjector(new Module(), new CamelModuleWithRouteTypes(Route.class));

        Thread.sleep(15000);
    }
}
