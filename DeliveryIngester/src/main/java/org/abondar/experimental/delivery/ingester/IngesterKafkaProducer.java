package org.abondar.experimental.delivery.ingester;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.kafka.client.producer.KafkaProducer;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.kafka.client.producer.KafkaProducerRecord;

import java.util.HashMap;
import java.util.Map;

import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.CURRENT_DELIVERY_FIELD;
import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.CURRENT_DESCRIPTION_FIELD;
import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.DELIVERED_DAILY_FIELD;
import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.DEVICE_ID_FIELD;
import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.DISTANCE_FIELD;
import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.KAFKA_PORT;
import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.KAFKA_TOPIC;
import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.SERVER_HOST;


public class IngesterKafkaProducer {




    public static KafkaProducer<String, JsonObject> initProducer(Vertx vertx){
        return KafkaProducer.create(vertx,configProducer());
    }

    private static Map<String,String> configProducer(){
        Map<String,String> config = new HashMap<>();
        config.put("bootstrap.servers",SERVER_HOST+":"+KAFKA_PORT);
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "io.vertx.kafka.client.serialization.JsonObjectSerializer");
        config.put("acks", "1");

        return config;
    }

    public static KafkaProducerRecord<String, JsonObject> makeRecord(JsonObject payload){
        var deviceId = payload.getString(DEVICE_ID_FIELD);
        var recordData = new JsonObject();

        recordData.put(DEVICE_ID_FIELD,deviceId);
        recordData.put(DELIVERED_DAILY_FIELD,payload.getString(DELIVERED_DAILY_FIELD));
        recordData.put(DISTANCE_FIELD,payload.getString(DISTANCE_FIELD));
        recordData.put(CURRENT_DELIVERY_FIELD,payload.getString(CURRENT_DELIVERY_FIELD));
        recordData.put(CURRENT_DESCRIPTION_FIELD,payload.getString(CURRENT_DESCRIPTION_FIELD));

        return KafkaProducerRecord.create(KAFKA_TOPIC,deviceId,recordData);
    }
}
