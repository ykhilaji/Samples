package providers;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class ProvidersModule extends AbstractModule {
    @Provides
    public Entity entity() {
        return new Entity();
    }
}
