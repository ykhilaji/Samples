### Mirror-maker usage example
1. Start 2 different zookeper servers:
	zookeeper-server-start zoo1.cfg
	zookeeper-server-start zoo2.cfg
2. Start 2 different kafka brokers:
	kafka-server-start broker.properties --override log.dirs=/tmp/kafka/1 --override zookeeper.connect=localhost:2181 --override listeners=PLAINTEXT://localhost:9092 --override offsets.topic.replication.factor=1 --override min.insync.replicas=1
	kafka-server-start broker.properties --override log.dirs=/tmp/kafka/2 --override zookeeper.connect=localhost:2182 --override listeners=PLAINTEXT://localhost:9093 --override offsets.topic.replication.factor=1 --override min.insync.replicas=1
3. Create topic on both 1-node-clusters (or use auto.create.topics.enable=true - this will create topic with DEFAULT properties):
	kafka-topics --create --topic test --zookeeper localhost:2181 --partitions 1 --replication-factor 1
	kafka-topics --create --topic test --zookeeper localhost:2182 --partitions 1 --replication-factor 1
4. Publish some messages to the created topic:
	kafka-console-producer --broker-list localhost:9092 --topic test
5. Start mirror-maker:
	kafka-mirror-maker --consumer.config consumer.properties --producer.config producer.properties --whitelist test --num.streams 1 --abort.on.send.failure true