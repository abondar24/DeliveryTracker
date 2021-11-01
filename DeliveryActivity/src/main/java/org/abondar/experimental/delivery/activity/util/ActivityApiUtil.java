package org.abondar.experimental.delivery.activity.util;

public class ActivityApiUtil {

    public static final int SERVER_PORT = 6000;
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String JSON_TYPE = "application/json";
    public static final String DEVICE_ID_FIELD = "deviceId";
    public static final String DELIVERED_FIELD = "delivered";
    public static final String DELIVERY_FIELD = "delivery";
    public static final String DISTANCE_FIELD = "distance";
    public static final String DEVICE_SYNC_FIELD = "deviceSync";
    public static final String TIMESTAMP_FIELD = "timestamp";
    public static final String DESCRIPTION_FIELD = "currentDescription";
    public static final String YEAR_PARAM = "year";
    public static final String MONTH_PARAM = "month";
    public static final String DAY_PARAM = "day";
    public static final String RANKING_ENDPOINT = "/day-distance-ranking";
    private static final String DEVICE_ID_PARAM = "/:" + DEVICE_ID_FIELD;
    public static final String TOTAL_ENDPOINT = DEVICE_ID_PARAM + "/total";
    private static final String YEAR_PATH_PARAM = "/:" + YEAR_PARAM;
    private static final String MONTH_PATH_PARAM = "/:" + MONTH_PARAM;
    private static final String DAY_PATH_PARAM = "/:" + DAY_PARAM;
    public static final String MONTH_ENDPOINT = DAY_PATH_PARAM + YEAR_PATH_PARAM + MONTH_PATH_PARAM;

    public static final String DAY_ENDPOINT = MONTH_ENDPOINT + DAY_PATH_PARAM;

    private ActivityApiUtil() {
    }


}
