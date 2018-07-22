package base;

import com.google.inject.Inject;

public class ServiceImpl implements Service {
    @Inject
    private Repository repository;

    public void doAction() {
        System.out.println("Inside service");
        repository.doAction();
    }
}
