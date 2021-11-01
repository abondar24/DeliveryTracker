package org.abondar.experimental.delivery.activity;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.vertx.junit5.VertxExtension;
import io.vertx.reactivex.core.Vertx;
import org.abondar.experimental.delivery.activity.verticle.ActivityApiVerticle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DELIVERED_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DEVICE_ID_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DISTANCE_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.RANKING_ENDPOINT;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.SERVER_PORT;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

@ExtendWith({VertxExtension.class})
public class ActivityApiTest {

    private static RequestSpecification spec;

    @BeforeAll
    public static void prepare(Vertx vertx) {
        spec = new RequestSpecBuilder()
                .addFilters(List.of(new ResponseLoggingFilter(), new RequestLoggingFilter()))
                .setBaseUri("http://localhost:" + SERVER_PORT)
                .build();

        var databaseService = new DatabaseTestServiceImpl();
        var verticle = new ActivityApiVerticle(databaseService);
        vertx.deployVerticle(verticle);
    }

    @Test
    public void getTotalTest() {
        given(spec)
                .contentType(ContentType.JSON)
                .get("/testId/total")
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .body(DELIVERED_FIELD, is(2))
                .body(DISTANCE_FIELD, is(4));
    }

    @Test
    public void getMonthTest() {
        given(spec)
                .contentType(ContentType.JSON)
                .get("/testId/2021/11")
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .body(DELIVERED_FIELD, is(2))
                .body(DISTANCE_FIELD, is(4));
    }


    @Test
    public void getMonthWrongTest() {
        given(spec)
                .contentType(ContentType.JSON)
                .get("/testId/2021/19")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void getMonthNotNumberTest() {
        given(spec)
                .contentType(ContentType.JSON)
                .get("/testId/2021/test")
                .then()
                .assertThat()
                .statusCode(400);
    }


    @Test
    public void getDayTest() {
        given(spec)
                .contentType(ContentType.JSON)
                .get("/testId/2021/11/01")
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .body(DELIVERED_FIELD, is(2))
                .body(DISTANCE_FIELD, is(4));
    }

    @Test
    public void getRankingTest() {
        given(spec)
                .contentType(ContentType.JSON)
                .get(RANKING_ENDPOINT)
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("size()",is(1))
                .body(DEVICE_ID_FIELD,hasItems("test123"))
                .body(DELIVERED_FIELD, hasItems(2))
                .body(DISTANCE_FIELD, hasItems(4));
    }
}
