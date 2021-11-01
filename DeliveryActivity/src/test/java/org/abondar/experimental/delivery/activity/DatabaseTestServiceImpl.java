package org.abondar.experimental.delivery.activity;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.RowSet;
import org.abondar.experimental.delivery.activity.service.DatabaseService;

import java.time.LocalDateTime;

import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DELIVERED_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DELIVERY_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DESCRIPTION_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DEVICE_ID_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DISTANCE_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.TIMESTAMP_FIELD;

public class DatabaseTestServiceImpl implements DatabaseService {
    @Override
    public Single<RowSet<Row>> insertDelivery(JsonObject data) {

        return Single.just(null);
    }

    @Override
    public Single<JsonObject> getDayDeliveries(String deviceId, String year, String month, String day) {
        var data = new JsonObject();
        data.put(DELIVERED_FIELD, 2);
        data.put(DISTANCE_FIELD, 4);
        return Single.just(data);
    }

    @Override
    public Single<JsonObject> getMonthDeliveries(String deviceId, String year, String month) {
        var data = new JsonObject();
        data.put(DELIVERED_FIELD, 2);
        data.put(DISTANCE_FIELD, 4);
        return Single.just(data);
    }

    @Override
    public Single<JsonObject> getTotalDeliveries(String deviceId) {
        var data = new JsonObject();
        data.put(DELIVERED_FIELD, 2);
        data.put(DISTANCE_FIELD, 4);
        return Single.just(data);
    }

    @Override
    public Single<JsonObject> getCurrentDelivery() {
        var data = new JsonObject();
        data.put(DELIVERY_FIELD, 123);
        data.put(DESCRIPTION_FIELD, "test");
        return Single.just(data);
    }

    @Override
    public Single<JsonArray> getDistanceRanking() {
        var rowJson = new JsonObject();
        rowJson.put(DEVICE_ID_FIELD, "test123");
        rowJson.put(DELIVERED_FIELD, 2);
        rowJson.put(DISTANCE_FIELD, 4);
        var data = new JsonArray();
        data.add(rowJson);

        return Single.just(data);
    }

    @Override
    public Single<JsonObject> getTodayUpdate(String deviceId) {
        var data = new JsonObject();
        data.put(DEVICE_ID_FIELD, deviceId);
        data.put(TIMESTAMP_FIELD, LocalDateTime.now().toString());
        data.put(DELIVERED_FIELD, 2);
        data.put(DISTANCE_FIELD, 4);

        return Single.just(data);
    }
}
