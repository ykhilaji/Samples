package jacksonsample;

public class Entity {
    private String field_one;
    private String field_two;
    private String field_three;

    public Entity() {
    }

    public Entity(String field_one, String field_two, String field_three) {
        this.field_one = field_one;
        this.field_two = field_two;
        this.field_three = field_three;
    }

    public String getField_one() {
        return field_one;
    }

    public void setField_one(String field_one) {
        this.field_one = field_one;
    }

    public String getField_two() {
        return field_two;
    }

    public void setField_two(String field_two) {
        this.field_two = field_two;
    }

    public String getField_three() {
        return field_three;
    }

    public void setField_three(String field_three) {
        this.field_three = field_three;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "field_one='" + field_one + '\'' +
                ", field_two='" + field_two + '\'' +
                ", field_three='" + field_three + '\'' +
                '}';
    }
}
