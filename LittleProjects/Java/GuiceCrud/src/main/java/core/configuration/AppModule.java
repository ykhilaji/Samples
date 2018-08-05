package core.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import core.aop.Log;
import core.aop.LogInterceptor;
import core.repository.UserRepository;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Log.class), new LogInterceptor());
    }

    @Provides
    @Singleton
    public UserRepository userRepository() {
        return new UserRepository();
    }
}
