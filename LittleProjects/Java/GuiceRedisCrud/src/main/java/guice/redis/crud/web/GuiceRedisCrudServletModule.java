package guice.redis.crud.web;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class GuiceRedisCrudServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        bind(RestController.class);
        serve("/*").with(GuiceContainer.class);
    }

    @Provides
    @Singleton
    public DefaultExceptionHandler defaultExceptionHandler() {
        return new DefaultExceptionHandler();
    }
}
