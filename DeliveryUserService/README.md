# Delivery User Service

Stores data about users and registers them.

## API
```yaml
http://localhost:5000 - server

- POST /register - register new user
    Request Body:
      {
      "username": "test",
      "password": "test",
      "email":"test@email.com",
      "garage": "test",
      "deviceId":"test123"
     }  
    
    Response:
     200 - User Registred
     409 - Register Error
     500 - Server Error

- POST /authenticate - authenticate user
  Request Body:
    {
    "username": "test",
    "password": "test",
  }

  Response:
    200 - Authentication successfull
    401 - Authentication failed


- GET /:username - fetch user by username
 
  Response:
    200 - User Found
    Body:
    {
      "username": "test",
      "password": "test",
      "email":"test@email.com",
      "garage": "test",
      "deviceId":"test123"
    }
    404 - User not found
    500 - Server Error


- PUT /:username - update user
   Request Body:
     {
     "email":"test@email.com",
     "garage": "test",
      }
  Response:
    200 - User updated
    500 - Server Error

- GET /device/:deviceId - fetch username by deviceId

  Response:
    200 - Device Found
    Body:
    {  
      "username": "test"
      "deviceId":"test123"
    }
    404 - Device not found
    500 - Server Error

```

## Build and run
```yaml
../gradlew :DeliveryUserService:clean :DeliveryUserService:build

java -jar build/libs/DeliveryUserService-1.0-SNAPSHOT-all.jar
```

