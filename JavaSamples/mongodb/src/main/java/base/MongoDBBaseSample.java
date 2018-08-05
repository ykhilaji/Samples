package base;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Arrays;

import static com.mongodb.client.model.Filters.eq;

public class MongoDBBaseSample {
    public static void main(String[] args) {
        MongoClient client = MongoClients.create(MongoClientSettings
                .builder()
                .applyToClusterSettings(builder -> {
                    builder.hosts(Arrays.asList(new ServerAddress("192.168.99.100", 27017)));
                }).build());

        MongoDatabase database = client.getDatabase("sample");
        MongoCollection<Document> collection = database.getCollection("sampleCollection");
        collection.createIndex(new Document("c1", 1)); // asc; -1 - desc

        Document document = new Document()
                .append("c1", "v1")
                .append("c2", "v2")
                .append("c3", "v3");


        System.out.println(document.toString());
        System.out.println(collection.countDocuments());
        collection.insertOne(document);
        System.out.println(collection.countDocuments());

        Document result = collection.find(eq("c1", "v1")).first();
        System.out.println(result.toJson());

        collection.updateOne(eq("c2", "v2"), new Document("$set", new Document("c2", "updatedValue")));
        result = collection.find(eq("c1", "v1")).first();
        System.out.println(result.toJson());

        collection.deleteMany(eq("c1", "v1"));
        System.out.println(collection.countDocuments());
    }
}
