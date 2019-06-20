# Monitoring for Zookeeper and Kafka

## Zookeeper
1. zookeeper-server-start zoo.cfg

## Kafka
1. export KAFKA_OPTS=-javaagent:<PATH>/jmx_exporter.jar=<LISTENING_PORT>:<PATH>/kafka_jmx.yml
2. kafka-server-start broker.properties

## Prometheus
docker run --name prometheus -v <PATH>/prometheus.yml:/etc/prometheus/prometheus.yml -p 9090:9090 -d prom/prometheus:v2.9.2

## Grafana
docker run --name grafana --net=host -p 3000:3000 -d grafana/grafana:6.0.2

Dashboard: https://grafana.com/dashboards/721