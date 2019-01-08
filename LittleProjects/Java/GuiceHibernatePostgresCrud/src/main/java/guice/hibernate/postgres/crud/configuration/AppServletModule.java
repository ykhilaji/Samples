package guice.hibernate.postgres.crud.configuration;

import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import guice.hibernate.postgres.crud.controller.Controller;


public class AppServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        bind(Controller.class);
        serve("/*").with(GuiceContainer.class);
    }
}