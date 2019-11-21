## Publisher side
1. Create new cluster: initdb -D /tmp/db1
2. pg_hba.conf ->  host     replication    replication_app 0.0.0.0/0     md5
3. Set wal_level = logical for db1
4. max_replication_slots should be equal or more than the number of subscribers that are supposed to connect to this publisher server
5. max_wal_senders >= max_replication_slots
6. pg_ctl -D /tmp/db1 -o "-p 5433" -l /tmp/db1/logs start (stop)
7. createdb -p 5433 -T template0 -e test_db
8. create user replication_app replication;  (or: createuser -p 5433 --replication replication_app)
9. create table test (a int); INSERT INTO test SELECT generate_series(0,100);
10. GRANT ALL PRIVILEGES ON test TO replication_app;
11. create publication my_pub for all tables;


## Subscriber side
1. Create new cluster: initdb -D /tmp/db2
2. pg_ctl -D /tmp/db2 -o "-p 5434" -l /tmp/db2/logs start (stop)
3. createdb -p 5434 -T template0 -e test_db
4. create table test (a int);
5. create subscription my_sub connection 'dbname=test_db user=replication_app host=localhost port=5433' publication my_pub;


## Test
psql -p 5434 test_db  
$: select * from test limit 10;

## Stop master
1. pg_ctl -D /tmp/db1 -o "-p 5433" -l /tmp/db1/logs stop
2. pg_ctl -D /tmp/db1 -o "-p 5433" -l /tmp/db1/logs start
3. INSERT INTO test SELECT generate_series(101,120);
4. subscriber will get new rows

## Stop subscriber
1. pg_ctl -D /tmp/db2 -o "-p 5434" -l /tmp/db2/logs stop
2. INSERT INTO test SELECT generate_series(121,140);
3. pg_ctl -D /tmp/db2 -o "-p 5434" -l /tmp/db2/logs start
4. subscriber will get new rows (recovered replication state of node 1 to 0/1699858)