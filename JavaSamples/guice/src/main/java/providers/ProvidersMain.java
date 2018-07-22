package providers;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ProvidersMain {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new ProvidersModule());

        Entity one = injector.getInstance(Entity.class);
        Entity two = injector.getInstance(Entity.class);

        one.doAction();
        two.doAction();

        System.out.println(one != two); // true
    }
}
