# Delivery Tracker

A small service tracking goods deliveries


## Structure

The service is split into several microservices

1. [Api](DeliveryApi/README.md) 
2. [Frontend](DeliveryFrontend/README.md)
3. [Ingester](DeliveryIngester/README.md)
4. [Notificator](DeliveryNotificator/README.md)
5. [User Service](DeliveryUserService/README.md)
6. [Activity Service](DeliveryActivity/README.md)
7. [Statistics Service](DeliveryStatService/README.md)
## Build and Run 

```yaml
./topic_setup.sh

./gradlew clean build

foreman start
```

## Note

- Integration tests randomly fail with connection refused exception
