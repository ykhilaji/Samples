## Redis sentinel

1. create separate config for each sentinel (sentinel1.conf, sentinel2.conf, sentinel3.conf) using default_sentinel.conf
2. create slave.conf ()
3. start: `docker-compose up`

Set some keys on master
Check that keys were replicated on slave
Kill master
Try to set keys on slave - now slave is master
Up master - now master is slave