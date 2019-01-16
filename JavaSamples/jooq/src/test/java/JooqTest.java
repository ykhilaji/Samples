import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.Column;
import javax.persistence.Id;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

import static org.jooq.impl.DSL.constraint;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The same table as usual:
 * create table if not exists entity (id serial primary key, value varchar(255) not null);
 */
public class JooqTest {
    final static String url = "jdbc:postgresql://192.168.99.100/postgres";
    final static String user = "postgres";
    final static String password = "";

    // JPA annotations are not necessary
    public static class Entity {
        @Id
        @Column(name = "id")
        private int id;
        @Column(name = "value", nullable = false)
        private String value;

        public Entity() {
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
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
            return id == entity.id &&
                    Objects.equals(value, entity.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, value);
        }

        @Override
        public String toString() {
            return "Entity{" +
                    "id=" + id +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    @BeforeAll
    public static void ddl() throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

            create.createTableIfNotExists("entity")
                    .column("id", SQLDataType.INTEGER.identity(true))
                    .column("value", SQLDataType.VARCHAR.length(255).nullable(false))
                    .constraints(
                            constraint("pk").primaryKey("id"))
                    .execute();
        }
    }

    @BeforeEach
    public void truncate() throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

            create.truncate("entity").execute();
        }
    }


    @Test
    public void simpleSelect() throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

            int fetch = create.resultQuery("select 1").fetchOne(0, Integer.class);
            assertEquals(1, fetch);
        }
    }

    @Test
    public void insertAndCount() throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

            create.transaction(tx -> {
                DSL.using(tx)
                        .insertInto(table("entity"), field("value"))
                        .values("val1")
                        .execute();
            });

            assertEquals(1, create.fetchCount(table("entity")));
        }
    }

    @Test
    public void insertAndSelectIntoPOJO() throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

            create.transaction(tx -> {
                DSL.using(tx)
                        .insertInto(table("entity"), field("value"))
                        .values("val1")
                        .values("val2")
                        .values("val3")
                        .values("val4")
                        .values("val5")
                        .execute();
            });

            Entity fetch = create
                    .selectFrom(table("entity"))
                    .where(field("value").eq("val3"))
                    .fetchOne().into(Entity.class);

            assertEquals("val3", fetch.getValue());
        }
    }
}
