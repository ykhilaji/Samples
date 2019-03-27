`create database test`
`create retention policy rp on test duration 1h replication 1`
`use test`
Commas are mandatory
`insert rp.metric, c1=1 c2=2,c3=3`
`drop series from metric`

Execute query each 1 hour and save results
```sql
CREATE CONTINUOUS QUERY "cnt_metric" ON "database_name"
BEGIN
  SELECT * INTO "database"."retention_policy"."metric_name" FROM "database"."retention_policy"."metric" GROUP BY time(1h)
END
```