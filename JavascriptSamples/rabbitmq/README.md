# RabbitMQ client sample usage
## Setup
Host: `192.168.99.100`

`docker run --name rabbitmq -p 5672:5672 -d rabbitmq:3.8.2-management-alpine`
## Start
```shell script
node src/BasicSample.js
node src/MultipleWorkers.js
node src/PublishSubscribe.js
node src/RoutingSample.js
node src/TopicSample.js
```