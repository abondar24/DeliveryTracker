package org.abondar.experimental.delivery.activity.service;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.RowSet;

public interface DatabaseService {

    Single<Row> insertDelivery(JsonObject data);

    Single<Row> getDailyDeliveries(String deviceId,String year,String month,String day);

    Single<Row> getMonthDeliveries(String deviceId,String year,String month);

    Single<Row> getTotalDeliveries(String deviceId);

    Single<Row> getCurrentDelivery(String deviceId);

    Single<RowSet<Row>> getDistanceRanking();

    Single<JsonObject> getTodayDeliveries(String deviceId);
}
