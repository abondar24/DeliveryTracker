package org.abondar.experimental.delivery.notificator;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.kafka.client.producer.KafkaProducer;
import io.vertx.reactivex.kafka.client.producer.KafkaProducerRecord;
import jakarta.mail.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static org.abondar.experimental.delivery.notificator.NotificatorUtil.DELIVERED_FIELD;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.DEVICE_ENDPOINT;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.DEVICE_ID_FIELD;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.DISTANCE_FIELD;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.EMAIL_PORT;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.KAFKA_TOPIC;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.USER_SERVICE_PORT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({VertxExtension.class})
public class NotificationIntegrationTest {

    private static final Map<String, String> PRODUCER_CONFIG = Map.of(
            "bootstrap.servers", "localhost:9092",
            "key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
            "value.serializer", "io.vertx.kafka.client.serialization.JsonObjectSerializer",
            "acks", "1"
    );
    private static KafkaProducer<String, JsonObject> testProducer;
    private static MockWebServer mockUserServer;
    private static GreenMail greenMail;

    @BeforeAll
    public static void init(Vertx vertx) throws Exception {
        testProducer = KafkaProducer.create(vertx, PRODUCER_CONFIG);
        mockUserServer = new MockWebServer();
        var userDispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                var resp = new MockResponse();
                resp.setHeader("content-type", "application/json");
                resp.setResponseCode(200);
                var body = new JsonObject();

                if (request.getPath().contains(DEVICE_ENDPOINT)) {
                    body.put("username", "test");
                }

                if (request.getPath().contains("/test") &&
                        !request.getPath().contains(DEVICE_ENDPOINT)) {
                    body.put("email", "user@mail.com");
                }

                resp.setBody(body.toString());
                return resp;
            }
        };

        mockUserServer.setDispatcher(userDispatcher);
        mockUserServer.start(USER_SERVICE_PORT);

        var setup = new ServerSetup(EMAIL_PORT, null, ServerSetup.PROTOCOL_SMTP);
        greenMail = new GreenMail(setup);

        greenMail.start();

        var verticle = new NotificatorVerticle();
        vertx.deployVerticle(verticle);

    }

    @AfterAll
    public static void shutdown() throws Exception {
        mockUserServer.shutdown();
        greenMail.stop();
    }


    @Test
    public void sendNotificationTest() throws Exception {
        JsonObject json = new JsonObject()
                .put(DEVICE_ID_FIELD, "test")
                .put(DELIVERED_FIELD, 100)
                .put(DISTANCE_FIELD, 100);


        testProducer.rxSend(KafkaProducerRecord.create(KAFKA_TOPIC, json.getString(DEVICE_ID_FIELD), json))
                .subscribe(ok -> assertEquals(KAFKA_TOPIC, ok.getTopic())
                );


        greenMail.waitForIncomingEmail(2000, 1);
        Message[] messages = greenMail.getReceivedMessages();

        assertEquals(1, messages.length);
        assertTrue(messages[0].getContent().toString().contains("You have delivered"));
    }


}
