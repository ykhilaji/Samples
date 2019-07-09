## Redis master-slave replication

1. Create slave.conf using default_slave.conf (just opy or rename file)
2. Start: `docker-compose up`
3. Set some keys on master
4. Check that keys were replicated on slave
