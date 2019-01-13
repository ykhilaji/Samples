package guice.camel.sample.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import guice.camel.sample.components.BeanComponent;
import guice.camel.sample.routes.Routes;
import org.apache.camel.RoutesBuilder;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        super.configure();
    }

    @Provides
    BeanComponent beanComponent() {
        return new BeanComponent();
    }

//    @Provides
//    RoutesBuilder routesBuilder() {
//        return new Routes();
//    }
}
