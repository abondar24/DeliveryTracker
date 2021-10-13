package org.abondar.experimental.delivery.ingester.util;

public class IngesterUtil {

    public static final int INGESTER_PORT = 4000;

    public static final int KAFKA_PORT = 9092;

    public static final int AMQP_PORT = 5672;

    public static final String SERVER_HOST = "localhost";

    public static final String AMQ_USER = "admin";

    public static final String CONTENT_TYPE = "application/json";
    public static final String AMQP_EVENT = "delivery-events";

    public static final String KAFKA_TOPIC = "delivery.data";

    public static final String DEVICE_ID_FIELD = "deviceId";

    public static final String DEVICE_SYNC_FIELD = "deviceSync";

    public static final String DELIVERED_DAILY_FIELD = "delivered";

    public static final String DISTANCE_FIELD = "distance";

    public static final String CURRENT_DELIVERY_FIELD = "currentDelivery";

    public static final String CURRENT_DESCRIPTION_FIELD = "currentDescription";



    private IngesterUtil(){}
}
