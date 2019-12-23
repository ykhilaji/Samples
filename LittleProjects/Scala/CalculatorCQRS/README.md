# Calculator CQRS

- akka http
- tapir for swagger doc
- doobie-postgresql
- fs2-rabbitmq

## Build
`sbt assembly`

## Docker
```text
docker run --name rabbitmq -p 5672:5672 rabbitmq -d
docker run --name postgresql -p 5432:5432 postgres -d
```

## Run
`java -jar CalculatorCQRS-assembly-0.1.jar`