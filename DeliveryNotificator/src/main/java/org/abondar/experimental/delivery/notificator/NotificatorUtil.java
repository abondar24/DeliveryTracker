package org.abondar.experimental.delivery.notificator;

import java.util.Map;

public class NotificatorUtil {

    public static final String SERVER_HOST = "localhost";
    public static final int KAFKA_PORT = 9092;
    public static final String KAFKA_TOPIC = "delivery.updates";
    public static final Map<String, String> KAFKA_CONFIG = Map.of("" +
                    "bootstrap.servers", SERVER_HOST + ":" + KAFKA_PORT,
            "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
            "value.deserializer", "io.vertx.kafka.client.serialization.JsonObjectDeserializer",
            "auto.offset.reset", "earliest",
            "enable.auto.commit", "true",
            "group", "notificator");
    public static final int EMAIL_PORT = 1025;
    public static final int USER_SERVICE_PORT = 5000;
    public static final String DEVICE_ENDPOINT = "/device";
    public static final String USERNAME_FIELD = "username";
    public static final String EMAIL_FIELD = "email";
    public static final String DELIVERED_FIELD = "delivered";
    public static final String DISTANCE_FIELD = "distance";
    public static final String DEVICE_ID_FIELD = "deviceId";
    public static final int DELIVERY_TARGET = 30;
    public static final int DELIVERY_MINUMUM = 10;
    //in km
    public static final int DISTANCE_TARGET = 100;
    public static final int DISTANCE_MINIMUM = 50;
    public static final String SYSTEM_ADDRESS = "deliveryTracker@globalDelivery.com";
    public static final String EMAIL_SUBJECT = "System Notification";

    private NotificatorUtil() {
    }
}
