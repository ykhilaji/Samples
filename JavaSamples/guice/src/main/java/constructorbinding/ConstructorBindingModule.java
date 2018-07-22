package constructorbinding;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class ConstructorBindingModule extends AbstractModule {
    @Override
    protected void configure() {
        try {
            bind(String.class).annotatedWith(Names.named("value")).toInstance("Some injected value");
            bind(Service.class).toConstructor(Service.class.getConstructor(Entity.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
