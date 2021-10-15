# Delivery Notificator

Service getting data from Kafka and sending email notifications 


## Build and run
```yaml
../gradlew :DeliveryNotificator:clean :DeliveryNotificator:build

java -jar build/libs/DeliveryNotificator-1.0-SNAPSHOT-all.jar
```
## Note
- Artemis and Kafka are required to run this service
- Integration tests randomly fail with connection refused exception
