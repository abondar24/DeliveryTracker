# Delivery Ingestor

Service accepting data from device via AMQP or http and posts to the corresponding Kafka Topic

## Access
```yaml
http://localhost:4000
```

## Build and run
```yaml
../topic_setup.sh

../gradlew :DeliveryIngester:clean :DeliveryIngester:build

java -jar build/libs/DeliveryIngester-1.0-SNAPSHOT-all.jar
```
## Note
- Integration tests randomly fail with connection refused exception
