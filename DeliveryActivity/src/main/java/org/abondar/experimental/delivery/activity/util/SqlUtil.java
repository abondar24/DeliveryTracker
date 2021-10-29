package org.abondar.experimental.delivery.activity.util;

public class SqlUtil {

    private SqlUtil(){};

    public static final String INSERT_QUERY = "INSERT INTO delivery VALUES($1,$2,current_timestamp,$3,$4,$5)";

    public static final String DELIVERIES_TODAY_QUERY = "SELECT current_timestamp, count(*)" +
            ",coalesce(sum(distance),0) FROM delivery WHERE (device_id=$1) AND " +
            "(date_trunc('day',sync_timestamp)=date_trunc('day'),current_timestamp))";

    public static final String TOTAL_DELIVERIES_QUERY = "SELECT count(*),sum(distance) FROM delivery WHERE (device_id=$1)";

    public static final String MONTH_DELIVERIES_QUERY = "SELECT count(*),sum(distance) FROM delivery " +
            "WHERE (device_id=$1) AND (date_trunc('month', sync_timestamp) = $2::timestamp)";

    public static final String DAY_DELIVERIES_QUERY = "SELECT count(*),sum(distance) FROM delivery " +
            "WHERE (device_id=$1) AND (date_trunc('day', sync_timestamp) = $2::timestamp)";

    public static final String DISTANCE_RANKING_QUERY = "SELECT device_id,count(*),sum(distance) as steps FROM delivery WHERE" +
            "(now() - sync_timestamp <= (interval '24 hours'))" +
            "GROUP BY device_id ORDER BY steps DESC";

    public static final String CURRENT_DELIVERY_QUERY = "SELECT delivery_id, desription FROM DELIVERY " +
            "ORDER BY sync_timestamp DESC LIMIT 1 ";
}
