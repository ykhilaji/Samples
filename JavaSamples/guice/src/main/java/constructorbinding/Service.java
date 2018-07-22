package constructorbinding;

public class Service {
    private Entity entity;

    public Service(Entity entity) {
        this.entity = entity;
    }

    public void doAction() {
        System.out.println(entity.getValue());
    }
}
