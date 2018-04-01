package avro;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.reflect.ReflectData;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.avro.AvroDataFormat;
import org.apache.camel.impl.DefaultCamelContext;

public class CamelAvroSample {
    public static class Entity {
        private String value;

        public Entity() {
        }

        public Entity(String value) {
            this.value = value;
        }
    }

    public static GenericRecord create(Schema schema, String data) {
        GenericRecord record = new GenericData.Record(schema);

        record.put("value", data);

        return record;
    }

    public static void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();

        final Schema schema = ReflectData.get().getSchema(Entity.class);
        final AvroDataFormat dataFormat = new AvroDataFormat(schema);
        dataFormat.setCamelContext(camelContext);
        dataFormat.setInstanceClassName("Entity");

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("timer:simple?period=1000")
                        .setBody()
                        .constant(create(schema, "some value"))
                        .marshal(dataFormat)
                        .to("stream:out");
            }
        });

        camelContext.start();

        Thread.sleep(3000);

        camelContext.stop();
    }
}
