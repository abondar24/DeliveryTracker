package org.abondar.experimental.delivery.notificator;

import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.impl.KafkaConsumerRecordImpl;
import io.vertx.lang.rx.TypeArg;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilterUtilTest {

    private KafkaConsumerRecord<String, JsonObject> createRecord(int delivered, int distance) {
        JsonObject json = new JsonObject()
                .put("deviceId", "test")
                .put("delivered", delivered)
                .put("distance", distance);

        var consumerRecord = new ConsumerRecord<>(NotificatorUtil.KAFKA_TOPIC,
                0, 0, "testKey", json);
        var delegate = new KafkaConsumerRecordImpl<>(consumerRecord);
        return new KafkaConsumerRecord<>(delegate, TypeArg.of(String.class),
                TypeArg.of(JsonObject.class));
    }


    @Test
    public void successFilterTest() {
        var record = createRecord(40, 100);
        assertTrue(FilterUtil.deliveryTargetSuccess(record));
    }

    @Test
    public void successFilterDistanceTest() {
        var record = createRecord(40, 60);
        assertFalse(FilterUtil.deliveryTargetSuccess(record));
    }

    @Test
    public void successFilterDeliveryTest() {
        var record = createRecord(10, 160);
        assertFalse(FilterUtil.deliveryTargetSuccess(record));
    }

    @Test
    public void failFilterTest() {
        var record = createRecord(5, 40);
        assertTrue(FilterUtil.deliveryTargetFail(record));
    }

    @Test
    public void failFilterDeliveryTest() {
        var record = createRecord(5, 50);
        assertFalse(FilterUtil.deliveryTargetFail(record));
    }

    @Test
    public void failFilterDistnaceTest() {
        var record = createRecord(10,5);
        assertFalse(FilterUtil.deliveryTargetFail(record));
    }
}
