package org.abondar.experimental.userservice;

import io.reactivex.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.abondar.experimental.delivery.userservice.service.MongoService;
import org.abondar.experimental.delivery.userservice.service.MongoServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.abondar.experimental.delivery.userservice.util.ApiUtil.DEVICE_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.EMAIL_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.GARAGE_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.PASSWORD_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.USERNAME_FIELD;
import static org.abondar.experimental.delivery.userservice.util.MongoUtil.USER_COLLECTION;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({VertxExtension.class})
public class MongoServiceIntegrationTest {

    private static MongoService mongoService;


    @BeforeAll
    public static void init(Vertx vertx) {
        mongoService = new MongoServiceImpl(vertx);
    }

    @AfterEach
    public void cleanCollection() {
        mongoService.cleanDb(USER_COLLECTION);
    }

    @Test
    public void registerUserTest(VertxTestContext testContext) {

        var res = registerTestUser();
        res.subscribe(
                testContext::completeNow,
                testContext::failNow);
    }

    @Test
    public void getUserTest(VertxTestContext testContext) {

        var user = registerTestUser();
        user.subscribe();


        var res = mongoService.getUser("test");
        res.subscribe(
                usr -> {
                    assertEquals(usr.getString(USERNAME_FIELD), "test");
                    testContext.completeNow();
                },
                testContext::failNow);
    }


    @Test
    public void updateUserTest(VertxTestContext testContext) {

        var user = registerTestUser();
        user.subscribe();

        var upd = new JsonObject();
        upd.put(EMAIL_FIELD,"emailUpdate");

        var res = mongoService.updateUser("test",upd);
        res.subscribe(
                testContext::completeNow,
                testContext::failNow);
    }

    @Test
    public void getDeviceIdTest(VertxTestContext testContext) {

        var user = registerTestUser();
        user.subscribe();

        var res = mongoService.getUser("test");
        res.subscribe(
                usr -> {
                    assertEquals(usr.getString(DEVICE_FIELD), "test");
                    testContext.completeNow();
                },
                testContext::failNow);
    }

    @Test
    public void authenticateUserTest(VertxTestContext testContext) {

        var user = registerTestUser();
        user.subscribe();

        var usr = new JsonObject();
        usr.put(USERNAME_FIELD,"test");
        usr.put(PASSWORD_FIELD,"test");

        var res = mongoService.authenticateUser(usr);
        res.subscribe(
                (auth)-> testContext.completeNow(),
                testContext::failNow);
    }

    private Completable registerTestUser() {
        var extraInfo = new JsonObject();
        var extraInfoSet = new JsonObject();
        extraInfoSet.put(EMAIL_FIELD, "testEmail");
        extraInfoSet.put(GARAGE_FIELD, "test");
        extraInfoSet.put(DEVICE_FIELD, "test");
        extraInfo.put("$set", extraInfoSet);
        return mongoService.registerUser("test", "test", extraInfo);
    }
}
