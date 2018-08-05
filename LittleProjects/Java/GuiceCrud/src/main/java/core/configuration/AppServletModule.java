package core.configuration;

import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import core.controller.AppController;

public class AppServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        bind(AppController.class);
        serve("/*").with(GuiceContainer.class);
    }
}
