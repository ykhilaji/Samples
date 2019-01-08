package guice.hibernate.postgres.crud;

import guice.hibernate.postgres.crud.model.Entity;
import guice.hibernate.postgres.crud.service.EntityService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EntityServiceTest {
    EntityService service = new EntityService();

    @Test
    public void serviceTest() {
        assertNotNull(service.save(new Entity()).getCreateTime());
        assertEquals(service.deleteAll(), 1);
    }
}
