#!/usr/bin/env bash

# list all available connector-plugins
curl localhost:8083/connector-plugins

# https://www.ibm.com/support/knowledgecenter/en/SSPT3X_4.2.5/com.ibm.swg.im.infosphere.biginsights.admin.doc/doc/admin_kafka_jdbc_sink.html
post new connector:
localhost:8083/connectors
{
    "name": "jdbc-sink-connector",
    "config": {
        "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
        "tasks.max": "1",
        "topics": "kafka.avro",
        "flush.size": "100",
        "connection.url": "jdbc:postgresql://localhost:5432/postgres",
        "connection.user": "postgres",
        "batch.size": "5",
        "insert.mode": "upsert"
    }
}


# get task status:
curl localhost:8083/connectors/jdbc-sink-connector/tasks
curl localhost:8083/connectors/jdbc-sink-connector/tasks/0/status
# restart task
post localhost:8083/connectors/jdbc-sink-connector/tasks/0/restart

# update connector config
put localhost:8083/connectors/jdbc-sink-connector/config

{
        "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
        "tasks.max": "1",
        "topics": "kafka.avro",
        "flush.size": "100",
        "connection.url": "jdbc:postgresql://localhost:5432/postgres",
        "connection.user": "postgres",
        "batch.size": "5",
        "insert.mode": "upsert",
        "key.converter": "org.apache.kafka.connect.storage.StringConverter",
        "key.converter.schema.registry.url": "http://schema-registry:8081",
        "value.converter": "io.confluent.connect.avro.AvroConverter",
        "value.converter.schema.registry.url": "http://schema-registry:8081",
        "auto.create": "true",
        "key.ignore": "true"
}