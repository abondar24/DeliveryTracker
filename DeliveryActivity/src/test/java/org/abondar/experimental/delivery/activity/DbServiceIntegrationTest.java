package org.abondar.experimental.delivery.activity;

import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.abondar.experimental.delivery.activity.service.DatabaseService;
import org.abondar.experimental.delivery.activity.service.DatabaseServiceImpl;
import org.abondar.experimental.delivery.activity.util.DbUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.Calendar;

import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DELIVERED_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DELIVERY_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DESCRIPTION_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DEVICE_ID_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DEVICE_SYNC_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DISTANCE_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.TIMESTAMP_FIELD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith({VertxExtension.class})
public class DbServiceIntegrationTest {

    private static DatabaseService service;

    private static PgPool pgPool;


    @BeforeAll
    public static void setup(Vertx vertx) {
        pgPool = PgPool.pool(vertx, DbUtil.PG_OPTS, new PoolOptions());
        service = new DatabaseServiceImpl(pgPool);
    }

    @AfterEach
    public void cleanDb() {
        pgPool.preparedQuery("DELETE from delivery")
                .rxExecute()
                .subscribe();
    }

    @Test
    public void insertTest(VertxTestContext testContext) {
        var data = new JsonObject();
        data.put(DEVICE_ID_FIELD, "testId");
        data.put(DEVICE_SYNC_FIELD, 1L);
        data.put(DISTANCE_FIELD, 2);
        data.put(DESCRIPTION_FIELD, "test");

        var res = service.insertDelivery(data);
        res.subscribe(
                rs -> testContext.completeNow(),
                testContext::failNow
        );
    }

    @Test
    public void getDailyDeliveriesTest(VertxTestContext testContext) throws Exception {
        insertDelivery();
        Thread.sleep(2000);

        var now = Calendar.getInstance();
        var year = String.valueOf(now.get(Calendar.YEAR));
        var month = String.valueOf(now.get(Calendar.MONTH) + 1);
        var day = String.valueOf(now.get(Calendar.DAY_OF_MONTH));


        var res = service.getDayDeliveries("testId", year, month, day);
        res.subscribe(
                data -> {
                    var count =data.getInteger(DELIVERED_FIELD);
                    var dist = data.getInteger(DISTANCE_FIELD);
                    assertEquals(1, count);
                    assertEquals(2, dist);
                    testContext.completeNow();
                },
                testContext::failNow
        );
    }


    @Test
    public void getMonthDeliveriesTest(VertxTestContext testContext) throws Exception {
        insertDelivery();
        Thread.sleep(2000);

        var now = Calendar.getInstance();
        var year = String.valueOf(now.get(Calendar.YEAR));
        var month = String.valueOf(now.get(Calendar.MONTH) + 1);

        var res = service.getMonthDeliveries("testId", year, month);
        res.subscribe(
                data -> {
                    var count =data.getInteger(DELIVERED_FIELD);
                    var dist = data.getInteger(DISTANCE_FIELD);
                    assertEquals(1, count);
                    assertEquals(2, dist);
                    testContext.completeNow();
                },
                testContext::failNow
        );
    }


    @Test
    public void getTotalDeliveriesTest(VertxTestContext testContext) throws Exception {
        insertDelivery();
        Thread.sleep(2000);

        var res = service.getTotalDeliveries("testId");
        res.subscribe(
                data -> {
                    var count =data.getInteger(DELIVERED_FIELD);
                    var dist = data.getInteger(DISTANCE_FIELD);
                    assertEquals(1, count);
                    assertEquals(2, dist);
                    testContext.completeNow();
                },
                testContext::failNow
        );
    }


    @Test
    public void getCurrentDeliveryTest(VertxTestContext testContext) throws Exception {
        insertDelivery();
        Thread.sleep(2000);

        var res = service.getCurrentDelivery();
        res.subscribe(
                data -> {
                    var delivery = data.getInteger(DELIVERY_FIELD);
                    var descr = data.getString(DESCRIPTION_FIELD);
                    assertNotNull(delivery);
                    assertEquals("test", descr);
                    testContext.completeNow();
                },
                testContext::failNow
        );
    }


    @Test
    public void getDistanceRankingTest(VertxTestContext testContext) throws Exception {
        insertDelivery();
        Thread.sleep(2000);

        var res = service.getDistanceRanking();
        res.subscribe(
                rank -> {
                    var size  = rank.size();
                    assertEquals(1,size);

                    var data = rank.getJsonObject(0);
                    var device = data.getString(DEVICE_ID_FIELD);
                    var count = data.getInteger(DELIVERED_FIELD);
                    var dist = data.getInteger(DISTANCE_FIELD);
                    assertEquals("testId",device);
                    assertEquals(1,count);
                    assertEquals(2, dist);
                    testContext.completeNow();
                },
                testContext::failNow
        );
    }


    @Test
    public void getTodayUpdateTest(VertxTestContext testContext) throws Exception {
        insertDelivery();
        Thread.sleep(2000);

        var res = service.getTodayUpdate("testId");
        res.subscribe(
                        data -> {
                            var deviceId = data.getString(DEVICE_ID_FIELD);
                            var count =data.getInteger(DELIVERED_FIELD);
                            var dist = data.getInteger(DISTANCE_FIELD);

                            assertEquals("testId",deviceId);
                            assertEquals(1,count);
                            assertEquals(2, dist);
                            testContext.completeNow();
                        },
                        testContext::failNow
                );
    }

    private void insertDelivery() {
        var data = new JsonObject();
        data.put(DEVICE_ID_FIELD, "testId");
        data.put(DEVICE_SYNC_FIELD, 1L);
        data.put(DISTANCE_FIELD, 2);
        data.put(DESCRIPTION_FIELD, "test");

        service.insertDelivery(data).subscribe();
    }
}
