package org.abondar.experimental.delivery.activity.util;

public class ActivityApiUtil {

    private ActivityApiUtil(){}

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

    private static final String DEVICE_ID_PARAM = "/:"+DEVICE_ID_FIELD;

    private static final String YEAR_PARAM = "/:year";

    private static final String MONTH_PARAM = "/:month";

    private static final String DAY_PARAM = "/:day";

    private static final String TOTAL_ENDPOINT = DEVICE_ID_PARAM+"/total";

    public static final String MONTH_ENDPOINT = DAY_PARAM+YEAR_PARAM+MONTH_PARAM;

    public static final String DAY_ENDPOINT = MONTH_ENDPOINT+DAY_PARAM;

    public static final String RANKING_ENDPOINT = "/ranking";




}
