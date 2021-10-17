# Delivery Notificator

Service getting data from Kafka and sending email notifications to users which failed with target or achieved it.

## Access
```yaml
localhost:4000
```

## Build and run
```yaml
../gradlew :DeliveryNotificator:clean :DeliveryNotificator:build

java -jar build/libs/DeliveryNotificator-1.0-SNAPSHOT-all.jar
```
