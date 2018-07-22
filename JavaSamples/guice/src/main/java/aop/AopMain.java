package aop;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class AopMain {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new AopModule());
        Service service = injector.getInstance(Service.class);

        service.doAction();
    }
}
