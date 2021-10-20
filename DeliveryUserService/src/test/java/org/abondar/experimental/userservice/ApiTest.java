package org.abondar.experimental.userservice;


import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.reactivex.core.Vertx;
import org.abondar.experimental.delivery.userservice.UserServiceVerticle;
import org.abondar.experimental.delivery.userservice.util.ApiUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.AUTH_ENDPOINT;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.DEVICE_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.EMAIL_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.GARAGE_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.PASSWORD_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.SERVER_PORT;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.USERNAME_FIELD;

@ExtendWith({VertxExtension.class})
public class ApiTest {

    private static RequestSpecification spec;

    @BeforeAll
    public static void initMockServer(Vertx vertx) throws Exception {
        spec = new RequestSpecBuilder()
                .addFilters(List.of(new ResponseLoggingFilter(), new RequestLoggingFilter()))
                .setBaseUri("http://localhost:" + SERVER_PORT)
                .build();

        var mongoService = new MongoServiceTestImpl();
        var verticle = new UserServiceVerticle(mongoService);
        vertx.deployVerticle(verticle);
    }


    @Test
    public void registerTest() {
        var json = new JsonObject()
                .put(USERNAME_FIELD, "test")
                .put(PASSWORD_FIELD, "test123")
                .put(EMAIL_FIELD, "test@email.me")
                .put(GARAGE_FIELD, "Munich")
                .put(DEVICE_FIELD, "a1b2c3");

        given(spec)
                .contentType(ContentType.JSON)
                .body(json.toString())
                .post(ApiUtil.REGISTER_ENDPOINT)
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void registerMissingFieldTest() {
        var json = new JsonObject()
                .put(USERNAME_FIELD, "test")
                .put(EMAIL_FIELD, "test@email.me")
                .put(GARAGE_FIELD, "Munich")
                .put(DEVICE_FIELD, "a1b2c3");

        given(spec)
                .contentType(ContentType.JSON)
                .body(json.toString())
                .post(ApiUtil.REGISTER_ENDPOINT)
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void registerWrongFieldTest() {
        var json = new JsonObject()
                .put(USERNAME_FIELD, "te st")
                .put(PASSWORD_FIELD, "")
                .put(EMAIL_FIELD, "test@email")
                .put(GARAGE_FIELD, "Munich")
                .put(DEVICE_FIELD, "a1b2c3");

        given(spec)
                .contentType(ContentType.JSON)
                .body(json.toString())
                .post(ApiUtil.REGISTER_ENDPOINT)
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void fetchUserTest() {
        given(spec)
                .contentType(ContentType.JSON)
                .get("/test")
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void updateUserTest() {
        var json = new JsonObject()
                .put(EMAIL_FIELD, "test@email.me")
                .put(GARAGE_FIELD, "Munich");

        given(spec)
                .contentType(ContentType.JSON)
                .body(json.toString())
                .put("/test")
                .then()
                .assertThat()
                .statusCode(200);
    }


    @Test
    public void fetchDeviceTest() {
        given(spec)
                .contentType(ContentType.JSON)
                .get("/device/a1b2c3")
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void authenticateTest() {
        var json = new JsonObject()
                .put(USERNAME_FIELD, "test")
                .put(PASSWORD_FIELD,"test");

        given(spec)
                .contentType(ContentType.JSON)
                .body(json.toString())
                .post(AUTH_ENDPOINT)
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void handleAuthenticationTest() {
        var json = new JsonObject()
                .put(USERNAME_FIELD, "test")
                .put(PASSWORD_FIELD,"tst");

        given(spec)
                .contentType(ContentType.JSON)
                .body(json.toString())
                .post(AUTH_ENDPOINT)
                .then()
                .assertThat()
                .statusCode(401);
    }

    @Test
    public void handleFetchErrorTest() {
        given(spec)
                .contentType(ContentType.JSON)
                .get("/errUser")
                .then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    public void handleRegistrationErrorTest() {
        var json = new JsonObject()
                .put(USERNAME_FIELD, "errUser")
                .put(PASSWORD_FIELD, "test123")
                .put(EMAIL_FIELD, "test@email.me")
                .put(GARAGE_FIELD, "Munich")
                .put(DEVICE_FIELD, "a1b2c3");

        given(spec)
                .contentType(ContentType.JSON)
                .body(json.toString())
                .post(ApiUtil.REGISTER_ENDPOINT)
                .then()
                .assertThat()
                .statusCode(409);
    }

    @Test
    public void handleServerErrorTest() {
        var json = new JsonObject()
                .put(USERNAME_FIELD, "err")
                .put(PASSWORD_FIELD, "test123")
                .put(EMAIL_FIELD, "test@email.me")
                .put(GARAGE_FIELD, "Munich")
                .put(DEVICE_FIELD, "a1b2c3");

        given(spec)
                .contentType(ContentType.JSON)
                .body(json.toString())
                .post(ApiUtil.REGISTER_ENDPOINT)
                .then()
                .assertThat()
                .statusCode(500);
    }
}
