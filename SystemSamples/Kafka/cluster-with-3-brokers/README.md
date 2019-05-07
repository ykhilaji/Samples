kafka-server-start broker.properties --override broker.id=<X> --override listeners=PLAINTEXT://host:port --override log.dirs=/tmp/kafka/logs-<X>

zookeeper-server-start zoo.cfg

kafka-topics --create --topic test --partitions 6 --replication-factor 2 --zookeeper localhost:2181

kafka-console-consumer --group console_consumer --bootstrap-server localhost:9092,localhost:9093,localhost:9094 --topic test

kafka-console-producer --broker-list localhost:9092,localhost:9093,localhost:9094 --topic test

kafka-consumer-groups --bootstrap-server localhost:9092,localhost:9093,localhost:9094 --group console_consumer --describe

kafka-topics --zookeeper localhost:2181 --topic test --describe

kafka-reassign-partitions --zookeeper localhost:2181 --topics-to-move-json-file topics-to-move.json --broker-list 0,1,2 --generate

kafka-reassign-partitions --zookeeper localhost:2181  --reassignment-json-file reassignment_configuration.json --bootstrap-server localhost:9092,localhost:9093,localhost:9094 --execute

kafka-reassign-partitions --zookeeper localhost:2181  --reassignment-json-file reassignment_configuration.json --bootstrap-server localhost:9092,localhost:9093,localhost:9094 --verify