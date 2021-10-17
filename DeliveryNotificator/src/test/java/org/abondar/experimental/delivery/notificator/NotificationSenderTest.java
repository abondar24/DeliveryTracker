package org.abondar.experimental.delivery.notificator;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.MailConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.kafka.client.consumer.impl.KafkaConsumerRecordImpl;
import io.vertx.lang.rx.TypeArg;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mail.MailClient;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.abondar.experimental.delivery.notificator.NotificatorUtil.DEVICE_ENDPOINT;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.EMAIL_PORT;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.USER_SERVICE_PORT;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({VertxExtension.class})
public class NotificationSenderTest {

    private static MockWebServer mockUserServer;

    private static NotificationSender sender;


    private static GreenMail greenMail;

    @BeforeAll
    public static void init(Vertx vertx) throws Exception {
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

        var webClient = WebClient.create(vertx);
        var mailClient = MailClient.createShared(vertx, new MailConfig()
                .setPort(EMAIL_PORT));

        sender = new NotificationSender(mailClient, webClient);
    }

    @AfterAll
    public static void shutdown() throws Exception {
        mockUserServer.shutdown();
        greenMail.stop();
    }

    @Test
    public void sendPositiveEmailTest(VertxTestContext testContext) {

        var record = getRecord();
        var res = sender.sendEmail(record, true);

        res.subscribe(
                ok -> {
                    assertEquals("user@mail.com", ok.getRecipients().get(0));
                    testContext.completeNow();
                },
                err -> testContext.failNow(err));
    }


    @Test
    public void sendNegativeEmailTest(VertxTestContext testContext) {

        var record = getRecord();
        var res = sender.sendEmail(record, false);

        res.subscribe(
                ok -> {
                    assertEquals("user@mail.com", ok.getRecipients().get(0));
                    testContext.completeNow();
                },
                err -> testContext.failNow(err));
    }

    private KafkaConsumerRecord<String, JsonObject> getRecord() {
        JsonObject json = new JsonObject()
                .put("deviceId", "test")
                .put("delivered", 100)
                .put("distance", 100);

        var consumerRecord = new ConsumerRecord<>(NotificatorUtil.KAFKA_TOPIC,
                0, 0, "testKey", json);
        var delegate = new KafkaConsumerRecordImpl<>(consumerRecord);
        return new KafkaConsumerRecord<>(delegate, TypeArg.of(String.class),
                TypeArg.of(JsonObject.class));
    }

}
