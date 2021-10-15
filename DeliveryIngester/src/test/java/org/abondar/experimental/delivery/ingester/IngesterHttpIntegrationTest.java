package org.abondar.experimental.delivery.ingester;


import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import org.abondar.experimental.delivery.ingester.util.IngesterUtil;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.DEVICE_ID_FIELD;
import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.INGESTER_PORT;
import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.SERVER_HOST;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class IngesterHttpIntegrationTest extends IngesterIntegrationTest {


    @Test
    public void testHttpIngester(Vertx vertx, VertxTestContext testContext) {
        var consumer = createConsumer(vertx);
        var message = createMessage();

        var requestSpecification = new RequestSpecBuilder()
                .addFilters(List.of(new ResponseLoggingFilter(), new RequestLoggingFilter()))
                .setBaseUri("http://" + SERVER_HOST + ":" + INGESTER_PORT)
                .build();

        vertx.rxDeployVerticle(new IngesterVerticle())
                .delay(1000, TimeUnit.MILLISECONDS, RxHelper.scheduler(vertx))
                .subscribe(ok -> testContext.completeNow(),
                        err -> testContext.failNow("Error"));


        given(requestSpecification)
                .contentType(ContentType.JSON)
                .body(message.encode())
                .post(IngesterUtil.INGEST_ENDPOINT)
                .then()
                .assertThat()
                .statusCode(200);

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
