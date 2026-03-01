#!/bin/bash

echo "Stopping and removing containers..."
docker compose down -v --rmi all

sleep 5

echo "Starting Docker services..."
docker compose up -d

sleep 5

echo "Waiting for Cassandra to be ready..."

timer=0
until docker exec cassandra-instagram-lite-docker \
  cqlsh -e "SELECT release_version FROM system.local;" >/dev/null 2>&1
do
  echo "[${timer}s] Cassandra not ready yet..."
  sleep 10
  timer=$((timer + 10))
done

echo "Cassandra is ready."

echo "Initializing Cassandra schema..."
docker exec -i cassandra-instagram-lite-docker \
cqlsh < scripts/cassandra-init.cql

echo "Initializing Kafka..."
./scripts/kafka-init.sh

echo "Setup complete."