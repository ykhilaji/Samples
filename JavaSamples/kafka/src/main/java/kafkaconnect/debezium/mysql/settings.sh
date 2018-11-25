#!/usr/bin/env bash

docker run --name mysql -v /Users/grifon/WORK/Samples/JavaSamples/kafka/src/main/java/kafkaconnect/debezium/mysql:/etc/mysql/conf.d -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql

CREATE USER 'replicator' IDENTIFIED BY 'password';

GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'replicator' IDENTIFIED BY 'password';

CREATE SCHEMA replicated_db;
CREATE TABLE replicated_db.entity(
  id INT PRIMARY KEY ,
  value text
);

post new connector:
localhost:8083/connectors
{
  "name": "replicated_db",
  "config": {
    "connector.class": "io.debezium.connector.mysql.MySqlConnector",
    "database.hostname": "localhost",
    "database.port": "3306",
    "database.user": "replicator",
    "database.password": "password",
    "database.server.id": "223344",
    "database.server.name": "mysql-server",
    "database.whitelist": "replicated_db",
    "database.history.kafka.bootstrap.servers": "localhost:9092",
    "database.history.kafka.topic": "dbhistory.replicated_db",
    "include.schema.changes": "true"
  }
}
