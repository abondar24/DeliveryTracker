# Delivery Tracker

A small service tracking goods deliveries


## Structure

The service is split into several microservices

1. [Api](DeliveryApi/README.md) 
2. [Frontend](DeliveryFrontend/README.md)
3. [Ingestor](DeliveryIngestor/README.md)

## Build and Run 

```yaml
./topic_setup.sh

./gradlew clean build

foreman start
```

## Note

Integration tests randomly fail with connection refused exception
