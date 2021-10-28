# DeliveryActivity

Consumes delivery data from Kafka topic and saves them in db.

## Activity Api

## Build and Run
```yaml
../gradlew clean build
../gradlew flywayMigrate

java -jar DeliveryActivity/build/libs/DeliveryActivity-1.0-SNAPSHOT-all.jar
```
