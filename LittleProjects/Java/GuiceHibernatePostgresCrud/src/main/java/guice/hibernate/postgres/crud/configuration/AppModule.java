package guice.hibernate.postgres.crud.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import guice.hibernate.postgres.crud.aop.Log;
import guice.hibernate.postgres.crud.aop.LogInterceptor;
import guice.hibernate.postgres.crud.repository.EntityRepository;
import guice.hibernate.postgres.crud.service.EntityService;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Log.class), new LogInterceptor());
    }

    @Provides
    @Singleton
    public EntityRepository entityRepository() {
        return new EntityRepository();
    }

    @Provides
    @Singleton
    public EntityService entityService() {
        return new EntityService();
    }
}
