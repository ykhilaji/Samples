# RabbitMQ
```
docker run --name rabbitmq -p 5672:5672 --hostname rabbit -d rabbitmq:3.7.17-management
docker exec -it rabbitmq /bin/bash

rabbitmqctl add_user rabbit
rabbitmqctl change_password rabbit rabbit
rabbitmqctl set_user_tags rabbit administrator

rabbitmqctl add_vhost test
rabbitmqctl set_permissions -p test rabbit ".*" ".*" ".*"

// This command will create queue and bind it with default exchange (with empty name and direct type ~~~ source="" destination="q1" routing_key="q1")
rabbitmqadmin declare queue --vhost=test name=q1 durable=false -u rabbit -p rabbit
rabbitmqadmin declare exchange --vhost=test name=e1 type=direct -u rabbit -p rabbit 

rabbitmqadmin declare binding --vhost=test name=b1 type=direct -u rabbit -p rabbit 
```