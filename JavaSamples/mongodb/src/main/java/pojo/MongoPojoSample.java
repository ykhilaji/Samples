package pojo;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.Arrays;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoPojoSample {
    public static void main(String[] args) {
        MongoClient client = MongoClients.create(MongoClientSettings
                .builder()
                .applyToClusterSettings(builder -> {
                    builder.hosts(Arrays.asList(new ServerAddress("192.168.99.100", 27017)));
                }).build());

        CodecRegistry pojoCodecRegistry = fromRegistries(com.mongodb.MongoClient.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        MongoDatabase database = client.getDatabase("sample");
        MongoCollection<Entity> collection = database.getCollection("pojo", Entity.class)
                .withCodecRegistry(pojoCodecRegistry);

        System.out.println(collection.countDocuments());
        collection.insertOne(new Entity(1, "val"));
        System.out.println(collection.countDocuments());

        collection.find().forEach((Consumer<Entity>) System.out::println);

        System.out.println(collection.find(eq("_id", 1)).first());

        collection.updateOne(eq("_id", 1), set("value", "updatedValue"));
        System.out.println(collection.find(eq("_id", 1)).first());

        collection.deleteOne(eq("_id", 1));
        System.out.println(collection.countDocuments());
    }
}
