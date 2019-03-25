package neo4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.*;

import java.util.List;

import static org.neo4j.driver.v1.Values.parameters;


import static org.junit.jupiter.api.Assertions.*;

public class BasicUsageJavaDriverTest {
    private static Driver driver;

    @BeforeAll
    public static void beforeAll() {
        driver = GraphDatabase.driver("bolt://192.168.99.100:7687");
    }

    @AfterAll
    public static void afterAll() {
        driver.close();
    }

    @AfterEach
    public void truncate() {
        try (Session session = driver.session()) {
            session.writeTransaction(transaction -> transaction.run("match (n) detach delete n"));
        }
    }

    @Test
    public void crud() {
        try (Session session = driver.session()) {
            Record entity = session.writeTransaction(transaction -> {
                StatementResult result = transaction.run("create (self:entity {id: $id, value: $value}) return self.id, self.value",
                        parameters("id", 1, "value", "some value"));

                return result.single();
            });

            assertEquals(entity.get("self.id").asInt(), 1);
            assertEquals(entity.get("self.value").asString(), "some value");

            Record address = session.writeTransaction(transaction -> {
                StatementResult result = transaction.run("create (self:address {country: $country, city: $city}) return self.country, self.city",
                        parameters("country", "some country", "city", "some city"));

                return result.single();
            });

            assertEquals(address.get("self.country").asString(), "some country");
            assertEquals(address.get("self.city").asString(), "some city");

            String reltype = session.writeTransaction(transaction -> {
                StatementResult result = transaction.run("match (e:entity), (a:address) create (e)-[l:live]->(a) return type(l)");

                return result.single().get(0).asString();
            });

            assertEquals(reltype, "live");

            List<Record> recordList = session.writeTransaction(transaction -> {
                StatementResult result = transaction.run("match (e: entity)-[:live]->(:address) return e");

                return result.list();
            });

            assertEquals(recordList.size(), 1);
        }
    }
}
