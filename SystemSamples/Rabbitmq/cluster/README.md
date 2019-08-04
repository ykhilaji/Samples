# Rabbitmq clustering
1. docker-compose up
2.
	- docker exec -it rabbitmq1 /bin/bash
	- docker exec -it rabbitmq2 /bin/bash
	- docker exec -it rabbitmq3 /bin/bash
3. On rabbitmq2 and rabbitmq3: rabbitmqctl stop_app
4. Optional: on rabbitmq2 and rabbitmq3: rabbimqctl reset
5. 
	- rabbit2:
		`rabbitmqctl join_cluster rabbit@rabbitmq1; rabbitmqctl start_app`
	- rabbit3:
		`rabbitmqctl join_cluster --ram rabbit@rabbitmq1; rabbitmqctl start_app`
6. 
	queues which names start with ha- will be mirrored on all nodes
	- `rabbitmqctl set_policy ha_all "^ha-\." '{"ha-mode":"all", "ha-sync-mode":"automatic"}'`
	- `rabbitmqadmin declare queue name=ha-q1 durable=true`
7.
	all nodes will mirror this message
	- `rabbitmqadmin publish exchange=amq.default routing_key=ha-q1 payload="msg1"`
	- `rabbitmqadmin get queue=ha-q1 ackmode=ack_requeue_false`
8. 
	- Stop rabbitmq2 (rabbitmqctl stop_app)
	- Send another messages: rabbitmqadmin publish exchange=amq.default routing_key=ha-q1 payload="msg2"
	- Check queue status: rabbitmqadmin list queues
	- Start rabbimq2 (rabbitmqctl start_app) - messages will be synchronized
9.
	queue ha-q1 will not be available due to master replica is offline:
	- Send message when all nodes online (rabbitmqadmin publish exchange=amq.default routing_key=ha-q1 payload="msg1")
	- Stop rabbitmq1
	- Try to list queues (count of messages will be undefined)
	- Try to publish another message (Message published but NOT routed)
	- Start rabbitmq1
	- Count of messages will be 0 (due to non-persistent delivery)
10.
	Reproduce step #9 but with persistent delivery
	- Send message when all nodes online (rabbitmqadmin publish exchange=amq.default routing_key=ha-q1 payload="msg1" properties="{\"delivery_mode\": 2}")
	- Stop rabbitmq1
	- Try to list queues (count of messages will be undefined)
	- Try to publish another message (Message published but NOT routed)
	- Start rabbitmq1
	- Count of messages will be 1 (messages which were published when rabbit1 was offline are lost - black-holed)