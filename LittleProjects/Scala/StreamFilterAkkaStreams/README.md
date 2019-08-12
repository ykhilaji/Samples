# Stream filter app

- Akka streams
- Kafka
- Aerospike

## Pipeline

- Read json data from source topic
- Check if entity with key exist in cache
- If entity exists -> send to target topic otherwise discard 