package org.abondar.experimental.delivery.activity.service;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.RowSet;

public interface DatabaseService {

    Single<RowSet<Row>> insertDelivery(JsonObject data);

    Single<JsonObject> getDayDeliveries(String deviceId, String year, String month, String day);

    Single<JsonObject> getMonthDeliveries(String deviceId,String year,String month);

    Single<JsonObject> getTotalDeliveries(String deviceId);

    Single<JsonObject> getCurrentDelivery();

    Single<RowSet<Row>> getDistanceRanking();

    Single<JsonObject> getTodayUpdate(String deviceId);
}
