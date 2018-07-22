package valuebindings;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class Service {
    @Inject
    @Named("some value")
    private String value;

    public void doAction() {
        System.out.println(value);
    }
}
