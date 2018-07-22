package aop;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class AopModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Service.class);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(AopAnnotation.class), new AopLogic());
    }
}
