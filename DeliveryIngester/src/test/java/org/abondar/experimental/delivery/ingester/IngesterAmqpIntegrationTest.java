package org.abondar.experimental.delivery.ingester;

import io.vertx.amqp.AmqpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.amqp.AmqpClient;
import io.vertx.reactivex.amqp.AmqpMessage;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import org.abondar.experimental.delivery.ingester.util.IngesterUtil;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.AMQP_PORT;
import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.AMQ_USER;
import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.DEVICE_ID_FIELD;
import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.SERVER_HOST;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class IngesterAmqpIntegrationTest extends IngesterIntegrationTest {


    @Test
    public void testAmqpIngester(Vertx vertx, VertxTestContext testContext) {
        var consumer = createConsumer(vertx);
        var message = createMessage();
        var amqpOpts = new AmqpClientOptions()
                .setHost(SERVER_HOST)
                .setPort(AMQP_PORT)
                .setUsername(AMQ_USER);

        var amqpClient = AmqpClient.create(vertx, amqpOpts);
        amqpClient.rxConnect()
                .flatMap(connection -> connection.rxCreateSender(IngesterUtil.AMQP_QUEUE))
                .subscribe(
                        sender -> {
                            AmqpMessage msg = AmqpMessage.create()
                                    .durable(true)
                                    .ttl(5000)
                                    .withJsonObjectAsBody(message).build();
                            sender.send(msg);
                        },
                        testContext::failNow);

        vertx.rxDeployVerticle(new IngesterVerticle())
                .delay(1000, TimeUnit.MILLISECONDS, RxHelper.scheduler(vertx))
                .subscribe(ok -> testContext.completeNow(),
                        err -> testContext.failNow("Error"));


        consumer.subscribe(IngesterUtil.KAFKA_TOPIC)
                .toFlowable()
                .subscribe(
                        record -> testContext.verify(() -> {
                            JsonObject json = record.value();
                            assertEquals("id", json.getString(DEVICE_ID_FIELD));
                            testContext.completeNow();
                        }),
                        testContext::failNow);
    }
}
