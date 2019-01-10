package guice.redis.crud.web;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import guice.redis.crud.configuration.GuiceRedisCrudModule;

public class ContextListener extends GuiceServletContextListener {
    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new GuiceRedisCrudModule(), new GuiceRedisCrudServletModule());
    }
}
