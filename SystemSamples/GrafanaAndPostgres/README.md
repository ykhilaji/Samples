```sql
create table time_series(
  "time" timestamp default now(),
  value numeric(38) default 0
);
```

```sql
insert into time_series(value) select floor(random() * 100000 + 1) from generate_series(1, 10000);
```

```sql
SELECT
  floor(extract(epoch from "time")/0.5)*0.5 AS "time",
  avg(value) AS "average",
  max(value) AS "max",
  min(value) AS "min"
FROM time_series
GROUP BY 1
ORDER BY 1
```

