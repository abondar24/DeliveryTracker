# Delivery Ingestor

Accepts data from device via AMQP or http and posts to the corresponding Kafka Topic

## Access
```yaml
http://localhost:4000
```

## Build and run
```yaml
../topic_setup.sh

../gradlew :DeliveryIngester:clean :DeliveryIngestr:build

java -jar build/libs/DeliveryIngester-1.0-SNAPSHOT-all.jar
```
## Note
- Artemis and Kafka are required to run this service
- Integration tests randomly fail with connection refused exception
