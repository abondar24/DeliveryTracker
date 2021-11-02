package org.abondar.experimental.delivery.activity;

import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import io.vertx.reactivex.kafka.client.producer.KafkaProducer;
import io.vertx.reactivex.kafka.client.producer.KafkaProducerRecord;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.abondar.experimental.delivery.activity.util.DbUtil;
import org.abondar.experimental.delivery.activity.verticle.EventVerticle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DESCRIPTION_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DEVICE_ID_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DEVICE_SYNC_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DISTANCE_FIELD;
import static org.abondar.experimental.delivery.activity.util.KafkaUtil.CONSUMER_CONFIG;
import static org.abondar.experimental.delivery.activity.util.KafkaUtil.KAFKA_CONSUMER_TOPIC;
import static org.abondar.experimental.delivery.activity.util.KafkaUtil.KAFKA_PRODUCER_TOPIC;
import static org.abondar.experimental.delivery.activity.util.KafkaUtil.PRODUCER_CONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith({VertxExtension.class})
public class EventVerticleIntegrationTest {

    private static KafkaProducer<String, JsonObject> testProducer;

    private static KafkaConsumer<String, JsonObject> testConsumer;

    private static PgPool pgPool;


    @BeforeAll
    public static void setup(Vertx vertx) {
        pgPool = PgPool.pool(vertx, DbUtil.PG_OPTS, new PoolOptions());

        testProducer = KafkaProducer.create(vertx, PRODUCER_CONFIG);
        testConsumer = KafkaConsumer.create(vertx, CONSUMER_CONFIG);

        var verticle = new EventVerticle();
        vertx.deployVerticle(verticle);
    }

    @AfterEach
    public void cleanDb() {
        pgPool.preparedQuery("DELETE from delivery")
                .rxExecute()
                .subscribe();
    }

    @Test
    public void processEventTest(VertxTestContext testContext) throws Exception {
        var message = new JsonObject();
        message.put(DEVICE_SYNC_FIELD, 1);
        message.put(DEVICE_ID_FIELD, "id");
        message.put(DISTANCE_FIELD, 29);
        message.put(DESCRIPTION_FIELD, "curr");

        testProducer.rxSend(KafkaProducerRecord.create(KAFKA_CONSUMER_TOPIC, message.getString(DEVICE_ID_FIELD), message))
                .subscribe(ok-> assertEquals(KAFKA_CONSUMER_TOPIC,ok.getTopic()),
                        testContext::failNow
                );

        Thread.sleep(2000);
        testConsumer.subscribe(KAFKA_PRODUCER_TOPIC)
                .toFlowable()
                .subscribe(res-> {assertNotNull(res.key());
                        testContext.completeNow();
                        },
                        testContext::failNow);

            Thread.sleep(2000);


        pgPool.preparedQuery("SELECT device_id,distance from delivery")
                .rxExecute()
                .map(rs -> rs.iterator().next())
                .subscribe(row -> {
                            var id = row.getString(0);
                            var dist = row.getInteger(1);
                            assertEquals(message.getString(DEVICE_ID_FIELD), id);
                            assertEquals(message.getInteger(DISTANCE_FIELD),dist);
                            testContext.completeNow();
                        },
                        testContext::failNow);



    }
}
