package org.abondar.experimental.delivery.stat;

import java.util.Map;

public class KafkaUtil {

    private KafkaUtil(){}

    public static final String KAFKA_HOST = "localhost";

    public static final int KAFKA_PORT  = 9092;

    public static final String DATA_TOPIC = "delivery.data";

    public static final String UPDATE_TOPIC = "delivery.updates";

    public static final String STAT_ACTIVITY_TOPIC = "stat.activity.updates";

    public static final String STAT_GARAGE_TREND_TOPIC = "stat.garage-trend.updates";

    public static final String THROUGHPUT_TOPIC = "stat.throughput";

    public static final Map<String,String> PRODUCER_CONFIG = Map.of(
            "bootstrap.servers",KAFKA_HOST+":"+KAFKA_PORT,
            "key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
            "value.serializer", "io.vertx.kafka.client.serialization.JsonObjectSerializer",
            "acks","1"
    );

    public static Map<String,String> consumerConfig(String group){
       return Map.of(
                "bootstrap.servers",KAFKA_HOST+":"+KAFKA_PORT,
                "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
                "value.deserializer", "io.vertx.kafka.client.serialization.JsonObjectDeserializer",
                "auto.offset.reset", "earliest",
                "enable.auto.commit", "true",
                "group.id",group);
    }
}
