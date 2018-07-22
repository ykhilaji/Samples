package constructorbinding;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ConstructorBindingMain {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new ConstructorBindingModule());
        Service service = injector.getInstance(Service.class);

        service.doAction();
    }
}
