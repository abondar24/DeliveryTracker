package org.abondar.experimental.delivery.api.util;


import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.reactivex.core.Vertx;
import org.abondar.experimental.delivery.api.DeliveryApiVerticle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static io.restassured.RestAssured.given;

@ExtendWith({VertxExtension.class})
public class DeliveryApiTest {

    private static RequestSpecification spec;
    private static MockWebServer mockWebServer;

    @BeforeAll
    public static void prepare(Vertx vertx) throws Exception {
        spec = new RequestSpecBuilder()
                .addFilters(List.of(new ResponseLoggingFilter(), new RequestLoggingFilter()))
                .setBaseUri("http://localhost:8000/")
                .setBasePath("/api/v1")
                .build();
        mockWebServer = new MockWebServer();


        Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                var resp = new MockResponse();
                resp.setHeader("content-type", "application/json");
                resp.setResponseCode(200);

                if (request.getPath().contains(ApiUtil.AUTH_ENDPOINT)) {
                    var body = new JsonObject();
                    body.put("username","test");
                    body.put("password","test123");
                    body.put("deviceId","123");
                    resp.setBody(body.toString());
                }

                if (request.getPath().contains("/test")) {
                    var body = new JsonObject();
                    body.put("deviceId","123");
                    resp.setBody(body.toString());
                }

                return resp;
            }
        };

        mockWebServer.setDispatcher(dispatcher);
        mockWebServer.start(5000);

       var verticle = new DeliveryApiVerticle();
        vertx.deployVerticle(verticle);
    }

    @AfterAll
    public static void shutdown() throws Exception{
        mockWebServer.shutdown();
    }

    @Test
    public void registerTest() throws Exception {
        var json = new JsonObject()
                .put("username", "test")
                .put("password", "test123")
                .put("email", "test@email.me")
                .put("garage", "Munich")
                .put("deviceId", "a1b2c3");

        given(spec)
                .contentType(ContentType.JSON)
                .body(json.toString())
                .post(ApiUtil.REGISTER_ENDPOINT)
                .then()
                .assertThat()
                .statusCode(200);

    }


    @Test
    public void tokenTest() {

        var json = new JsonObject()
                .put("username", "test")
                .put("password", "test123");
        given(spec)
                .contentType(ContentType.JSON)
                .body(json.toString())
                .post(ApiUtil.TOKEN_ENDPOINT)
                .then()
                .assertThat()
                .statusCode(200);
    }


    @Test
    public void tokenUnauthorizedTest() {

        var json = new JsonObject()
                .put("username", "arsen")
                .put("password", "test1");
        given(spec)
                .contentType(ContentType.JSON)
                .body(json.toString())
                .post(ApiUtil.TOKEN_ENDPOINT)
                .then()
                .assertThat()
                .statusCode(401);
    }


}
