# Delivery Ingestor

Accepts data from device via AMQP or http and posts to the corresponding Kafka Topic

## Access
```yaml
http://localhost:4000
```

## Build and run
```yaml
../gradlew :DeliveryIngestor:clean :DeliveryIngestor:build

java -jar build/libs/DeliveryIngestor-1.0-SNAPSHOT-all.jar
```
