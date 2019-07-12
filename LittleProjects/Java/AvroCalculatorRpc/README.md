# Avro Calculator RPC

## Gradle avro plugin
https://github.com/commercehub-oss/gradle-avro-plugin

## Build image
`docker build -t <image name> .`

## Start server
`docker-compose up --scale server=3`

# Start client
`docker run -it <image name> java -jar /app/application.jar client <server host> <server port>` 