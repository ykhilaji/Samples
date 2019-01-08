package guice.hibernate.postgres.crud.configuration;

import java.util.ResourceBundle;

public class Configuration {
    public final static ResourceBundle bundle = ResourceBundle.getBundle("config");

    public final static String schema = bundle.getString("schema");
}
