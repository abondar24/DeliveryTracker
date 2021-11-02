package org.abondar.experimental.delivery.stat;

import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.kafka.admin.KafkaAdminClient;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import io.vertx.reactivex.kafka.client.producer.KafkaProducer;
import io.vertx.reactivex.kafka.client.producer.KafkaProducerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.List;

import static org.abondar.experimental.delivery.stat.util.FieldUtil.COUNT_FIELD;
import static org.abondar.experimental.delivery.stat.util.FieldUtil.SECONDS_FIELD;
import static org.abondar.experimental.delivery.stat.util.KafkaUtil.DATA_TOPIC;
import static org.abondar.experimental.delivery.stat.util.KafkaUtil.PRODUCER_CONFIG;
import static org.abondar.experimental.delivery.stat.util.KafkaUtil.STAT_ACTIVITY_TOPIC;
import static org.abondar.experimental.delivery.stat.util.KafkaUtil.STAT_GARAGE_TREND_TOPIC;
import static org.abondar.experimental.delivery.stat.util.KafkaUtil.THROUGHPUT_TOPIC;
import static org.abondar.experimental.delivery.stat.util.KafkaUtil.UPDATE_TOPIC;
import static org.abondar.experimental.delivery.stat.util.KafkaUtil.consumerConfig;
import static org.abondar.experimental.delivery.stat.util.UserServiceUtil.USER_SERVICE_PORT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({VertxExtension.class})
public class StatIntegrationTest {

    private static KafkaProducer<String, JsonObject> testProducer;

    private static KafkaConsumer<String, JsonObject> testConsumer;

    private static MockWebServer mockUserServer;

    @BeforeAll
    public static void setup(Vertx vertx) throws Exception {
        testProducer = KafkaProducer.create(vertx, PRODUCER_CONFIG);


        mockUserServer = new MockWebServer();
        var userDispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                var resp = new MockResponse();
                resp.setHeader("content-type", "application/json");
                resp.setResponseCode(200);
                var body = new JsonObject();

                if (request.getPath().contains("/device")) {
                    body.put("deviceId", "test123");
                }

                if (request.getPath().contains("/test")) {
                    body.put("username", "test");
                }

                resp.setBody(body.toString());
                return resp;
            }
        };

        mockUserServer.setDispatcher(userDispatcher);
        mockUserServer.start(USER_SERVICE_PORT);

        vertx.deployVerticle(new StatVerticle());
    }

    @AfterAll
    public static void shutdown() throws Exception {
        mockUserServer.shutdown();
    }


    @Test
    public void throughputCalcTest(VertxTestContext testContext,Vertx vertx) throws Exception {
        JsonObject json = new JsonObject();
        json.put("deviceID", "test123");
        json.put("distance", 2);
        json.put("deviceSync", 1);
        json.put("description", "test");

        testConsumer = KafkaConsumer.create(vertx, consumerConfig("stat-test"));
        testProducer.send(KafkaProducerRecord.create(DATA_TOPIC, json.getString("deviceID"), json));
        testConsumer.subscribe(THROUGHPUT_TOPIC)
                .toFlowable()
                .subscribe(rec -> {
                            var val = rec.value();
                            var seconds = val.getInteger(SECONDS_FIELD);
                            var count = val.getInteger(COUNT_FIELD);

                            assertTrue(seconds>=5);
                            assertTrue(count>=0);

                            testContext.completeNow();
                        },
                        testContext::failNow);

    }


    @Test
    public void updateCalcTest(VertxTestContext testContext,Vertx vertx) throws Exception {
        JsonObject json = new JsonObject();
        json.put("deviceID", "test123");
        json.put("timestamp", LocalDateTime.now().toString());
        json.put("delivered", 2);
        json.put("distance", 2);

        testConsumer = KafkaConsumer.create(vertx, consumerConfig("stat-test"));
        testProducer.send(KafkaProducerRecord.create(UPDATE_TOPIC, json.getString("deviceID"), json));
        testConsumer.subscribe(STAT_ACTIVITY_TOPIC)
                .toFlowable()
                .subscribe(rec -> {
                            var val = rec.value();
                            var deviceId = val.getString("deviceId");
                            var distance = val.getInteger("distance");

                            assertEquals("test123", deviceId);
                            assertTrue(distance>=2);

                            testContext.completeNow();
                        },
                        testContext::failNow);
    }

    @Test
    public void trendCalcTest(VertxTestContext testContext,Vertx vertx) throws Exception {
        JsonObject json = new JsonObject();
        json.put("deviceID", "test123");
        json.put("timestamp", LocalDateTime.now().toString());
        json.put("delivered", 2);
        json.put("distance", 2);
        testConsumer = KafkaConsumer.create(vertx, consumerConfig("stat-test"));
        testProducer.send(KafkaProducerRecord.create(UPDATE_TOPIC, json.getString("deviceID"), json));

        Thread.sleep(2000);

        testConsumer.subscribe(STAT_GARAGE_TREND_TOPIC)
                .toFlowable()
                .subscribe(rec -> {
                            var val = rec.value();
                            var seconds = val.getInteger("seconds");
                            var updates = val.getInteger("updates");
                            var delivered = val.getInteger("delivered");
                            var distance = val.getInteger("distance");

                            assertTrue(seconds>=5);
                            assertTrue(updates>=1);
                            assertTrue(distance>=2);
                            assertTrue(delivered>=2);
                            testContext.completeNow();
                        },
                        testContext::failNow);
    }
}
