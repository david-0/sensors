#!/bin/bash

cd "$(dirname "$0")"

nohup ../kafka_2.11-2.1.0/bin/zookeeper-server-start.sh ../kafka_2.11-2.1.0/config/zookeeper.properties 2>&1 > logs/zookeeper-$(date +%Y%m%d-%H%M%S).log < /dev/zero &
nohup ../kafka_2.11-2.1.0/bin/kafka-server-start.sh ../kafka_2.11-2.1.0/config/server.properties 2>&1 > logs/server-$(date +%Y%m%d-%H%M%S).log < /dev/zero &

sudo nohup java -jar sensors-backend/target/sensors-backend-0.0.1-SNAPSHOT-jar-with-dependencies.jar 2>&1 > logs/backend-$(date +%Y%m%d-%H%M%S).log < /dev/zero &
