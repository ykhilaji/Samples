package base;

import com.google.inject.Inject;

public class RepositoryImpl implements Repository {
    @Inject
    private DataSource dataSource;

    public void doAction() {
        System.out.println("Inside repository");
        dataSource.doAction();
    }
}
