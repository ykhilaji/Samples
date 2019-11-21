## Master server
1. Create new cluster: initdb -D /tmp/db1
2. Set wal_level = logical or replica; max_wal_senders = 10; max_replication_slots = 10
3. pg_hba.conf: host    streaming_app    0.0.0.0/0    md5
4. pg_ctl -D /tmp/db1 -o "-p 5433" -l /tmp/db1/logs start
5. createuser -p 5433 --replication streaming_app
6. createdb -p 5433 -T template0 -e test_db
7. create table test (a int); INSERT INTO test SELECT generate_series(0,1000);
8. select * from pg_create_physical_replication_slot('slot1');

## Standby
1. pg_basebackup -p 5433 -D /tmp/db2  (in this sample this command will use the default user)
2. chmod 0700 /tmp/db2
3. optional [?]: after base backup comment wal_level and archiving command in postgres.conf for db2 (standby)
4. create recovery.conf in /tmp/db2
standby_mode = 'on'
primary_conninfo = 'host=localhost port=5433 user=streaming_app'
primary_slot_name = 'slot1'


## Stop master
1. pg_ctl -D /tmp/db1 -o "-p 5433" -l /tmp/db1/logs stop
2. pg_ctl -D /tmp/db1 -o "-p 5433" -l /tmp/db1/logs start
3. INSERT INTO test SELECT generate_series(0,1000);
4. standby will get new rows

## Stop standby
1. pg_ctl -D /tmp/db2 -o "-p 5434" -l /tmp/db2/logs stop
2. INSERT INTO test SELECT generate_series(0,1000);
3. pg_ctl -D /tmp/db2 -o "-p 5434" -l /tmp/db2/logs start
4. standby will get new rows
