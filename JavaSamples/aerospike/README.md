## Basic
docker run -v /Users/grifon/WORK/Samples/JavaSamples/aerospike/src/test/resources:/opt/aerospike/etc --name aerospike -p 3000:3000 -p 3001:3001 -p 3002:3002 -p 3003:3003 -d aerospike/aerospike-server asd --foreground --config-file /opt/aerospike/etc/aerospike.conf
