#!/bin/bash

../../kafka_2.13-3.0.0/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic delivery.data
../../kafka_2.13-3.0.0/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic delivery.updates
../../kafka_2.13-3.0.0/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic stat.activity.updates
../../kafka_2.13-3.0.0/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic stat.garage-trend.updates
../../kafka_2.13-3.0.0/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic stat.throughput

