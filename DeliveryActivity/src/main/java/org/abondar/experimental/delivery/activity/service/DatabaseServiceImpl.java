package org.abondar.experimental.delivery.activity.service;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.RowSet;
import io.vertx.reactivex.sqlclient.Tuple;


import java.time.LocalDateTime;

import static org.abondar.experimental.delivery.activity.util.ActivityUtil.DESCRIPTION_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityUtil.DELIVERED_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityUtil.DEVICE_ID_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityUtil.DEVICE_SYNC_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityUtil.DISTANCE_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityUtil.TIMESTAMP_FIELD;
import static org.abondar.experimental.delivery.activity.util.SqlUtil.CURRENT_DELIVERY_QUERY;
import static org.abondar.experimental.delivery.activity.util.SqlUtil.DAY_DELIVERIES_QUERY;
import static org.abondar.experimental.delivery.activity.util.SqlUtil.DELIVERIES_TODAY_QUERY;
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
    public Single<Row> insertDelivery(JsonObject data) {

        var params = Tuple.of(
                data.getString(DEVICE_ID_FIELD),
                data.getString(DEVICE_SYNC_FIELD),
                data.getInteger(DELIVERED_FIELD),
                data.getInteger(DISTANCE_FIELD),
                data.getString(DESCRIPTION_FIELD)
        );

      return pgPool.preparedQuery(INSERT_QUERY)
                .rxExecute(params)
              .map(rs->rs.iterator().next());
    }

    @Override
    public Single<Row> getDailyDeliveries(String deviceId,String year,String month,String day) {
        var dateTime = LocalDateTime.of(Integer.parseInt(year),
                Integer.parseInt(month),
                Integer.parseInt(day), 0,0);

        var params = Tuple.of(deviceId,dateTime);

        return pgPool.preparedQuery(DAY_DELIVERIES_QUERY)
                .rxExecute(params)
                .map(rs-> rs.iterator().next());
    }

    @Override
    public Single<Row> getMonthDeliveries(String deviceId,String year,String month) {
          var dateTime = LocalDateTime.of(Integer.parseInt(year),
                  Integer.parseInt(month),1,0,0);

          var params = Tuple.of(deviceId,dateTime);

          return pgPool.preparedQuery(MONTH_DELIVERIES_QUERY)
                  .rxExecute(params)
                  .map(rs-> rs.iterator().next());
    }

    @Override
    public Single<Row> getTotalDeliveries(String deviceId) {
        var param = Tuple.of(deviceId);

        return pgPool.preparedQuery(TOTAL_DELIVERIES_QUERY)
                .rxExecute(param)
                .map(rs->rs.iterator().next());
    }

    @Override
    public Single<Row> getCurrentDelivery(String deviceId) {
        var param = Tuple.of(deviceId);

        return pgPool.preparedQuery(CURRENT_DELIVERY_QUERY)
                .rxExecute(param)
                .map(rs->rs.iterator().next());
    }

    @Override
    public Single<RowSet<Row>> getDistanceRanking() {
        return pgPool.preparedQuery(DISTANCE_RANKING_QUERY)
                .rxExecute();
    }

    @Override
    public Single<JsonObject> getTodayDeliveries(String deviceId) {
        var param = Tuple.of(deviceId);
        return pgPool.preparedQuery(DELIVERIES_TODAY_QUERY)
                .rxExecute(param)
                .map(rs->rs.iterator().next())
                .map(row-> {
                    var data = new JsonObject();
                    data.put(DEVICE_ID_FIELD,deviceId);
                    data.put(TIMESTAMP_FIELD,row.getTemporal(0).toString());
                    data.put(DELIVERED_FIELD,row.getLong(1));
                    data.put(DISTANCE_FIELD,row.getLong(2));

                    return data;
                   }
                );
    }
}
