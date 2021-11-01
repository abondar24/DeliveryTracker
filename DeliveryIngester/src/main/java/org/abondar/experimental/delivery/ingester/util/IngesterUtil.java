package org.abondar.experimental.delivery.ingester.util;

public class IngesterUtil {

    public static final int INGESTER_PORT = 4000;

    public static final String INGEST_ENDPOINT = "/ingest";

    public static final int KAFKA_PORT = 9092;

    public static final int AMQP_PORT = 5672;

    public static final String SERVER_HOST = "localhost";

    public static final String AMQ_USER = "admin";

    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final String AMQP_QUEUE = "delivery-events";

    public static final String KAFKA_TOPIC = "delivery.data";

    public static final String DEVICE_ID_FIELD = "deviceId";

    public static final String DEVICE_SYNC_FIELD = "deviceSync";

    public static final String DISTANCE_FIELD = "distance";


    public static final String DESCRIPTION_FIELD = "currentDescription";



    private IngesterUtil(){}
}
