package org.abondar.experimental.delivery.activity.util;

import java.util.Map;

public class KafkaUtil {

    private KafkaUtil(){}

    public static final int KAFKA_PORT = 9092;

    public static final String KAFKA_HOST = "localhost";

    public static final String KAFKA_CONSUMER_TOPIC = "delivery.data";

    public static final String KAFKA_PRODUCER_TOPIC = "delivery.updates";

    public static final Map<String,String> PRODUCER_CONFIG = Map.of(
            "bootstrap.servers",KAFKA_HOST+":"+KAFKA_PORT,
            "key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
            "value.serializer", "io.vertx.kafka.client.serialization.JsonObjectSerializer",
            "acks","1"
    );

    public static final Map<String,String> CONSUMER_CONFIG = Map.of(
            "bootstrap.servers",KAFKA_HOST+":"+KAFKA_PORT,
            "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
            "value.deserializer", "io.vertx.kafka.client.serialization.JsonObjectDeserializer",
            "auto.offset.reset", "earliest"
    );

}
