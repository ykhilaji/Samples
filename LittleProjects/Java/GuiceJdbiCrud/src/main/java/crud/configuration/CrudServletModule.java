package crud.configuration;

import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import crud.controller.RestController;

public class CrudServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        bind(RestController.class);
        serve("/*").with(GuiceContainer.class);
    }
}
