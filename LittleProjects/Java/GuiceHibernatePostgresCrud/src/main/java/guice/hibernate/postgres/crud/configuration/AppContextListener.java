package guice.hibernate.postgres.crud.configuration;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class AppContextListener extends GuiceServletContextListener {
    protected Injector getInjector() {
        return Guice.createInjector(new AppModule(), new AppServletModule());
    }
}