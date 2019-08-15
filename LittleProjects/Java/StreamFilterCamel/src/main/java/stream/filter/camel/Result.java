package stream.filter.camel;

public class Result {
    public final boolean exist;
    public final Entity entity; // mutable object, but for this case it's ok

    public Result(boolean exist, Entity entity) {
        this.exist = exist;
        this.entity = entity;
    }

    @Override
    public String toString() {
        return "Result{" +
                "exist=" + exist +
                ", entity=" + entity +
                '}';
    }
}
