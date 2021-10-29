package org.abondar.experimental.delivery.activity.util;

public class KafkaUtil {

    private KafkaUtil(){}

    public static final int KAFKA_PORT = 9092;

    public static final String KAFKA_HOST = "localhost";

    public static final String KAFKA_CONSUMER_TOPIC = "delivery.data";

    public static final String KAFKA_PRODCUCER_TOPIC = "delivery.updates";

}
