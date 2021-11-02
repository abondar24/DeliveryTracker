# Delivery API

Backend api of delivery tracking service

## API description
```
API URL - localhost:8000/v1/api

1. POST localhost:8000/v1/api/register - register a new user
        Body: {
                "username":"test",
                "password":"test123",
                "email":"test@email.me",
                "garage":"Munich",
                "deviceId":"123"
        }  
       Response:  
            200 - User registred
            502 - Server error

2. POST localhost:8000/v1/api/token - authorize user and generate JWT token
        Body: {
          "username":"test",
          "password":"test123"
        }
        Response:
          200 - JWT token
          Body: someToken123
          401 - Unauthorized
          504 - Circuit breaker enabled

3. GET localhost:8000/v1/api/:username - fetch user data
        Headers:
           Authroization Bearer <jwt-token>
        Response:
          200 - User details
          Body: {
            "username":"test",
            "password":"test123",
            "email":"test@email.me",
            "garage":"Munich",
            "deviceId":"123"
          }
          401 - Unauthorized
          403 - Unauthenticated
          502 - Server error

4. PUT localhost:8000/v1/api/:username - fetch user data
        Headers:
          Authroization Bearer <jwt-token>
        Body: {
          "username":"test",
          "password":"test123",
          "email":"test@email.me",
          "garage":"Munich",
          "deviceId":"123"
        }
        Response:
          200 - Updated user details
          Body: {
            "username":"test",
            "password":"test123",
            "email":"test@email.me",
            "garage":"Munich",
            "deviceId":"123"
          }
          401 - Unauthorized
          403 - Unauthenticated
          502 - Server error

5. GET localhost:8000/v1/api/:username/total - fetch total number of delivered goods and distance
        Headers:
          Authroization Bearer <jwt-token>
        Response:
          200 - Delivery data 
          Body: {
                 "delivered":"123",
                "distance":"456"
          }
          401 - Unauthorized
          403 - Unauthenticated
          502 - Server error

6. GET localhost:8000/v1/api/:username/:year/:month - fetch number of delivered goods and distance per month
        Headers:
          Authroization Bearer <jwt-token>
        Response:
          200 - Delivery data
          Body: {
            "delivered":"123",
            "distance":"456"
          }
          401 - Unauthorized
          403 - Unauthenticated
          502 - Server error

7. GET localhost:8000/v1/api/:username/:year/:month/:day - fetch number of delivered goods and distance per day
        Headers:
          Authroization Bearer <jwt-token>
        Response:
          200 - Delivery data
          Body: {
            "delivered":"123",
            "distance":"456"
          }
          401 - Unauthorized
          403 - Unauthenticated
          502 - Server error

8. GET localhost:8000/v1/api/:username/current - get current delivery
        Headers:
             Authroization Bearer <jwt-token>
        Response:
        200 - Delivery data
        Body: {
           "delivery": "delivery123",
           "description": "description"
        }
        401 - Unauthorized
        403 - Unauthenticated
        502 - Server error
```


## Build and Run
```yaml
 ../gradlew :DeliveryApi:clean :DeliveryApi:build

 java -jar build/libs/DeliveryApi-1.0-SNAPSHOT-all.jar
```

## Note

- public_key.pem and private_key.pem certificates must be stored in jar dir
