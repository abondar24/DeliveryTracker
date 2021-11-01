package org.abondar.experimental.delivery.activity.service;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.RowSet;
import io.vertx.reactivex.sqlclient.Tuple;

import java.time.DateTimeException;
import java.time.LocalDateTime;

import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DELIVERED_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DELIVERY_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DESCRIPTION_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DEVICE_ID_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DEVICE_SYNC_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DISTANCE_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.TIMESTAMP_FIELD;
import static org.abondar.experimental.delivery.activity.util.SqlUtil.CURRENT_DELIVERY_QUERY;
import static org.abondar.experimental.delivery.activity.util.SqlUtil.DAY_DELIVERIES_QUERY;
import static org.abondar.experimental.delivery.activity.util.SqlUtil.DELIVERIES_UPDATE_TODAY_QUERY;
import static org.abondar.experimental.delivery.activity.util.SqlUtil.DISTANCE_RANKING_QUERY;
import static org.abondar.experimental.delivery.activity.util.SqlUtil.INSERT_QUERY;
import static org.abondar.experimental.delivery.activity.util.SqlUtil.MONTH_DELIVERIES_QUERY;
import static org.abondar.experimental.delivery.activity.util.SqlUtil.TOTAL_DELIVERIES_QUERY;

public class DatabaseServiceImpl implements DatabaseService {

    private final PgPool pgPool;

    public DatabaseServiceImpl(PgPool pgPool) {
        this.pgPool = pgPool;
    }

    @Override
    public Single<RowSet<Row>> insertDelivery(JsonObject data) {

        var params = Tuple.of(
                data.getString(DEVICE_ID_FIELD),
                data.getInteger(DEVICE_SYNC_FIELD),
                data.getInteger(DISTANCE_FIELD),
                data.getString(DESCRIPTION_FIELD)
        );

        return pgPool.preparedQuery(INSERT_QUERY)
                .rxExecute(params);
    }

    @Override
    public Single<JsonObject> getDayDeliveries(String deviceId, String year, String month, String day) throws DateTimeException,NumberFormatException {
        var dateTime = LocalDateTime.of(Integer.parseInt(year),
                Integer.parseInt(month),
                Integer.parseInt(day), 0, 0);

        var params = Tuple.of(deviceId, dateTime);

        return getStat(DAY_DELIVERIES_QUERY, params);
    }

    @Override
    public Single<JsonObject> getMonthDeliveries(String deviceId, String year, String month) throws DateTimeException,NumberFormatException{
        var dateTime = LocalDateTime.of(Integer.parseInt(year),
                Integer.parseInt(month), 1, 0, 0);

        var params = Tuple.of(deviceId, dateTime);

        return getStat(MONTH_DELIVERIES_QUERY, params);
    }

    @Override
    public Single<JsonObject> getTotalDeliveries(String deviceId) throws DateTimeException,NumberFormatException {
        var param = Tuple.of(deviceId);

        return getStat(TOTAL_DELIVERIES_QUERY, param);
    }

    private Single<JsonObject> getStat(String query, Tuple params) {
        return pgPool.preparedQuery(query)
                .rxExecute(params)
                .map(rs -> rs.iterator().next())
                .map(row -> {
                            var data = new JsonObject();
                            data.put(DELIVERED_FIELD, row.getInteger(0));
                            data.put(DISTANCE_FIELD, row.getInteger(1));

                            return data;
                        }
                );
    }

    @Override
    public Single<JsonObject> getCurrentDelivery() {

        return pgPool.preparedQuery(CURRENT_DELIVERY_QUERY)
                .rxExecute()
                .map(rs -> rs.iterator().next())
                .map(row -> {
                            var data = new JsonObject();
                            data.put(DELIVERY_FIELD, row.getInteger(0));
                            data.put(DESCRIPTION_FIELD, row.getString(1));

                            return data;
                        }
                );

    }

    @Override
    public Single<JsonArray> getDistanceRanking() {
        return pgPool.preparedQuery(DISTANCE_RANKING_QUERY)
                .rxExecute()
                .map(rs -> {
                    var rank = new JsonArray();
                    rs.forEach(row -> {
                        var rowJson = new JsonObject();
                        rowJson.put(DEVICE_ID_FIELD, row.getString(0));
                        rowJson.put(DELIVERED_FIELD, row.getInteger(1));
                        rowJson.put(DISTANCE_FIELD, row.getInteger(2));
                        rank.add(rowJson);
                    });
                    return rank;
                });
    }

    @Override
    public Single<JsonObject> getTodayUpdate(String deviceId) {
        var param = Tuple.of(deviceId);
        return pgPool.preparedQuery(DELIVERIES_UPDATE_TODAY_QUERY)
                .rxExecute(param)
                .map(rs -> rs.iterator().next())
                .map(row -> {
                            var data = new JsonObject();
                            data.put(DEVICE_ID_FIELD, deviceId);
                            data.put(TIMESTAMP_FIELD, row.getTemporal(0).toString());
                            data.put(DELIVERED_FIELD, row.getInteger(1));
                            data.put(DISTANCE_FIELD, row.getInteger(2));

                            return data;
                        }
                );
    }
}
