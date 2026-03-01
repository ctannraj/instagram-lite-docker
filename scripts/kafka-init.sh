#!/bin/bash

echo "Creating Kafka topic..."

docker exec kafka-instagram-lite-docker \
kafka-topics --create \
--if-not-exists \
--topic post-events \
--bootstrap-server localhost:9092 \
--partitions 1 \
--replication-factor 1

echo "Kafka topic ready."