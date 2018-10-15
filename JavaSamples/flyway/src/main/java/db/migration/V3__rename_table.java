package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Statement;

public class V3__rename_table extends BaseJavaMigration {
    public void migrate(Context context) throws Exception {
        try (Statement rename = context.getConnection().createStatement()) {
            try {
                String tableName = context.getConfiguration().getPlaceholders().get("tableName");
                String newTableName = context.getConfiguration().getPlaceholders().get("newTableName");
                rename.execute(String.format("alter table %s rename to %s", tableName,newTableName)); // we can't parameterize table names
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
