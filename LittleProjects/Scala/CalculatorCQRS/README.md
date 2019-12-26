# Calculator CQRS

- akka http
- tapir for swagger doc
- doobie-postgresql
- fs2-rabbitmq
- flyway
- prometheus
- zipkin

```text
localhost:8080/docs - swagger docs
localhost:8080/metrics - prometheus metrics
```

## Build
`sbt assembly`

## Docker
```text
docker run --name rabbitmq -p 5672:5672 rabbitmq -d
docker run --name postgresql -p 5432:5432 postgres -d
docker run --name zipkin -p 9411:9411 openzipkin/zipkin:2.19.2 -d
```

## Run
`java -jar CalculatorCQRS-assembly-0.1.jar`