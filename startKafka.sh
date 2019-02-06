#!/bin/bash

nohup ../kafka_2.11-2.1.0/bin/zookeeper-server-start.sh ../kafka_2.11-2.1.0/config/zookeeper.properties 2>&1 > /dev/null < /dev/zero &
nohup ../kafka_2.11-2.1.0/bin/kafka-server-start.sh ../kafka_2.11-2.1.0/config/server.properties 2>&1 > /dev/null < /dev/zero &
