zookeeper-server-start zoo.cfg

kafka-topics --create --topic test --partitions 6 --replication-factor 2 --zookeeper localhost:2181

kafka-console-consumer --group console_consumer --bootstrap-server localhost:9092,localhost:9093,localhost:9094 --topic test

kafka-console-producer --broker-list localhost:9092,localhost:9093,localhost:9094 --topic test

kafka-consumer-groups --bootstrap-server localhost:9092,localhost:9093,localhost:9094 --group console_consumer --describe

kafka-topics --zookeeper localhost:2181 --topic test --describe