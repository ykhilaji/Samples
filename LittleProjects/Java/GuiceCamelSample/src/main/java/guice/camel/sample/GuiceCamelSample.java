package guice.camel.sample;

import com.google.inject.Guice;
import guice.camel.sample.configuration.AppModule;
import guice.camel.sample.routes.Routes;
import org.apache.camel.guice.CamelModuleWithRouteTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class GuiceCamelSample {
    private static final Logger logger = LogManager.getLogger("GuiceCamelSample");

    public static void main(String[] args) throws InterruptedException {
        logger.info("Start GuiceCamelSample");
        Guice.createInjector(new AppModule(), new CamelModuleWithRouteTypes(Routes.class));
        Thread.sleep(15000);
        logger.info("Stop GuiceCamelSample");
    }
}
