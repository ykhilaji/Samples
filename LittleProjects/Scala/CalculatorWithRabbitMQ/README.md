# Calculator

- cats (+ cats-effect)
- fs2-rabbitmq (rpc)
- doobie (postgresql)

## Start docker images
```text
docker run --name=postgres -p 5432:5432 -d postgres
docker run --name=rabbitmq -p 5672:5672 -p 15672:15672 -d rabbitmq:3.8.2-management-alpine
```


## Build
`sbt assembly`

## Run
`java -jar CalculatorWithRabbitMQ-assembly-0.1`