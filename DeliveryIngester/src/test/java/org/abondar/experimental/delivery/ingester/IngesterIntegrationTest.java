package org.abondar.experimental.delivery.ingester;


import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import org.abondar.experimental.delivery.ingester.util.IngesterUtil;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;

import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.KAFKA_PORT;
import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.SERVER_HOST;

@ExtendWith({VertxExtension.class})
public class IngesterIntegrationTest {


    protected KafkaConsumer<String, JsonObject> createConsumer(Vertx vertx){
        Map<String, String> config = Map.of("bootstrap.servers", SERVER_HOST + ":" + KAFKA_PORT,
                "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
                "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return KafkaConsumer.create(vertx, config);

    }

    protected JsonObject createMessage(){
        var message = new JsonObject();
        message.put("content-type", IngesterUtil.CONTENT_TYPE_JSON);
        message.put(IngesterUtil.DEVICE_SYNC_FIELD, "sync");
        message.put(IngesterUtil.DEVICE_ID_FIELD, "id");
        message.put(IngesterUtil.DISTANCE_FIELD, 24);
        message.put(IngesterUtil.DELIVERED_DAILY_FIELD, 7);
        message.put(IngesterUtil.CURRENT_DESCRIPTION_FIELD, "curr");
        message.put(IngesterUtil.CURRENT_DELIVERY_FIELD, "curId");

        return message;
    };
}
