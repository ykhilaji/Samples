package valuebindings;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ValueBindingsMain {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new ValueBndingsModule());
        Service service = injector.getInstance(Service.class);

        service.doAction();
    }
}
