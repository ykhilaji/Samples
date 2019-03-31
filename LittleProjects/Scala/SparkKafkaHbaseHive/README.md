Pipeline Kafka -> Spark -> HBase -> Hive (hbase handler in external table)

1. Start dfs (start-dfs.sh)
2. Start metastore database (~ docker start postgres)
3. Start metastore thrift server (hive --service metastore)
4. Start hbase (start-hbase.sh)
5. Create hbase table (hbase shell -> ```create 'entity', 'info'```)
6. Create hive table (
```sql
    create external table entity_hbase(id bigint, value string, ts bigint)
    stored by 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
     with serdeproperties ('hbase.columns.mapping'=':key,info:value,info:timestamp')
     tblproperties('hbase.table.name'='entity');
```
)