import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.reflect.ReflectData;

import java.io.File;
import java.io.IOException;

public class BasicSample {
    public static class Entity {
        private String value;

        public Entity() {
        }

        public Entity(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entity entity = (Entity) o;

            return value != null ? value.equals(entity.value) : entity.value == null;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Entity{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }

    public static void main(String[] args) throws IOException {
        Schema schema = ReflectData.get().getSchema(Entity.class);
        GenericRecord entity = new GenericData.Record(schema);

        entity.put("value", "generated value");

        System.out.println(entity);

        DatumWriter<GenericRecord> writer = new GenericDatumWriter<GenericRecord>();
        DataFileWriter<GenericRecord> fileWriter = new DataFileWriter<GenericRecord>(writer);

        File file = new File("file.avro");

        fileWriter.create(schema, file);
        fileWriter.append(entity);
        fileWriter.close();
    }
}
