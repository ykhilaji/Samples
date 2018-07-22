package valuebindings;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class ValueBndingsModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Service.class);
        bind(String.class).annotatedWith(Names.named("some value")).toInstance("Injected value");
    }
}
