package annotation;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class AnnotationsMain {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new Module());
        Service service = injector.getInstance(Service.class);

        service.fromMySQL();
        service.fromPostgreSQL();
    }
}
