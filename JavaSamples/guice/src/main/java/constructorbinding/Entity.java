package constructorbinding;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class Entity {
    @Inject
    @Named("value")
    private String value;

    public String getValue() {
        return value;
    }
}
