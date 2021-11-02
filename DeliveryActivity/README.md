# DeliveryActivity

Consumes delivery data from Kafka topic and saves them in db.

## Activity Api
```yaml
http://localhost:6000 - server
 - GET /:deviceId/total - get total number of deliveries
   200 - Deliveries Found
    Body:
      {
        "delivered": 2
        "distance": 4
      }
   404 - No deliveries found
   500 - Server Error
    
 - GET /:deviceId/:year/:month - get deliveries per month
   200 - Deliveries Found
    Body:
      {
        "delivered": 2
        "distance": 4
      }
   400 - Wrong date     
   404 - No deliveries found
   500 - Server Error

 - GET /:deviceId/:year/:month/:day - get deliveries per day
   200 - Deliveries Found
    Body:
      {
        "delivered": 2
        "distance": 4
      }
    400 - Wrong date    
    404 - No deliveries found
    500 - Server Error
  - GET /day-distance-ranking - get deliveries ranking by distance
    200 - Deliveries Found
      Body:
        {
           "deviceId": "deviceID"
           "delivered": 2
           "distance": 4
        }
    404 - No deliveries found
    500 - Server Error

```
## Build and Run
```yaml
../gradlew :DeliveryActivity:clean :DeliveryActivity:build
../gradlew :DeliveryActivity:flywayMigrate

java -jar build/libs/DeliveryActivity-1.0-SNAPSHOT-all.jar
```
