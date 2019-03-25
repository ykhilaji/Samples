package neo4j;

import org.junit.jupiter.api.*;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BasicUsageEmbeddedTest {
    private static GraphDatabaseFactory graphDbFactory;
    private static GraphDatabaseService graphDatabaseService;

    @BeforeAll
    public static void beforeAll() {
        graphDbFactory = new GraphDatabaseFactory();
        graphDatabaseService = graphDbFactory.newEmbeddedDatabase(new File("data/test"));
    }

    @AfterAll
    public static void afterAll() {
        graphDatabaseService.shutdown();
    }

    @AfterEach
    public void truncate() {
        graphDatabaseService.execute("MATCH (n) DETACH DELETE n");
    }

    @Test
    public void crud() {
        Transaction transaction = graphDatabaseService.beginTx();
        try {
            Node entity = graphDatabaseService.createNode(Label.label("entity"));
            entity.setProperty("id", 1);
            entity.setProperty("value", "some value");

            Node address = graphDatabaseService.createNode(Label.label("address"));
            address.setProperty("country", "country");
            address.setProperty("city", "city");

            entity.createRelationshipTo(address, RelationshipType.withName("live"));

            // should be executed in transaction too
            Node node = graphDatabaseService.findNode(Label.label("entity"), "id", 1);
            assertEquals(node.getProperty("value"), "some value");

            graphDatabaseService.execute("match (e: entity {id: 1}) set e.value = \"updated\"");
            Node updated = graphDatabaseService.findNode(Label.label("entity"), "id", 1);
            assertEquals(updated.getProperty("value"), "updated");

            Result result = graphDatabaseService.execute("match (a: address)<-[:live]-(e: entity) where e.id = 1 return a.country, a.city");
            Map<String, Object> map = result.next();
            assertEquals(map.get("a.country"), "country");
            assertEquals(map.get("a.city"), "city");

            transaction.success();
        } catch (Exception e) {
            transaction.failure();
            throw e;
        } finally {
            transaction.close();
        }
    }
}
