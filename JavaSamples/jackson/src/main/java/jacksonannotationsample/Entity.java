package jacksonannotationsample;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonRootName(value = "entity")
//@JsonSerialize(using = CustomSerializer.class)
//@JsonDeserialize(using = CustomDesirializer.class)
public class Entity {
    @JsonProperty(value = "annotatedId")
    private int id;
    @JsonRawValue
    private String field;
    @JsonIgnore
    private String value;

    public Entity() {
    }

    public Entity(int id, String field, String value) {
        this.id = id;
        this.field = field;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id +
                ", field='" + field + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
