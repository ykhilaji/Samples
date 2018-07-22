package annotation;

import com.google.inject.Inject;

public class ServiceImpl implements Service {
    @Inject @MySQL
    private Source mysql;
    private Source postgres;

    @Inject
    public ServiceImpl(@PostgreSQL Source postgres) {
        this.postgres = postgres;
    }

    public void fromMySQL() {
        mysql.doAction();
    }

    public void fromPostgreSQL() {
        postgres.doAction();
    }
}
