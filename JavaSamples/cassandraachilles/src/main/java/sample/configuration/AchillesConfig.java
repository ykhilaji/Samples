package sample.configuration;

import info.archinnov.achilles.annotations.CompileTimeConfig;
import info.archinnov.achilles.type.CassandraVersion;
import info.archinnov.achilles.type.strategy.ColumnMappingStrategy;
import info.archinnov.achilles.type.strategy.InsertStrategy;
import info.archinnov.achilles.type.strategy.NamingStrategy;

@CompileTimeConfig(cassandraVersion = CassandraVersion.CASSANDRA_3_11_0,
        insertStrategy = InsertStrategy.ALL_FIELDS,
        namingStrategy = NamingStrategy.SNAKE_CASE,
        columnMappingStrategy = ColumnMappingStrategy.EXPLICIT)
public interface AchillesConfig {

}