package org.abondar.experimental.delivery.notificator;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;

import static org.abondar.experimental.delivery.notificator.NotificatorUtil.DELIVERY_MINUMUM;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.DELIVERY_TARGET;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.DISTANCE_MINIMUM;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.DISTANCE_TARGET;

public class FilterUtil {

    private FilterUtil(){}

    public static boolean deliveryTargetSuccess(KafkaConsumerRecord<String, JsonObject> record) {
        var val = record.value();

        return val.getInteger("delivered") >= DELIVERY_TARGET &&
                val.getInteger("distance") >= DISTANCE_TARGET;
    }

    public static boolean deliveryTargetFail(KafkaConsumerRecord<String, JsonObject> record) {
        var val = record.value();

        return val.getInteger("delivered") < DELIVERY_MINUMUM &&
                val.getInteger("distance") < DISTANCE_MINIMUM;
    }
}
