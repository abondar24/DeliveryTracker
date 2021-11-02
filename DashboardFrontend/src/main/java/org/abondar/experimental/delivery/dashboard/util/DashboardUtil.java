package org.abondar.experimental.delivery.dashboard.util;

public class DashboardUtil {

    private DashboardUtil(){}

    public static final int SERVER_PORT = 8020;

    public static final int ACTIVITY_PORT = 6000;

    public static final int USER_SERVICE_PORT = 5000;

    public static final int RETRY_TIMEOUT = 5;

    public static final String ACTIVITY_HOST = "localhost";

    public static final String USER_SERVICE_HOST = "localhost";

    public static final String CONTENT_TYPE_HEADER = "Content-Type";

    public static final String JSON_TYPE = "application/json";

    public static final String DISTANCE_RANKING_ENDPOINT = "/day-distance-ranking";

    public static final String DEVICE_ENDPOINT = "/device";

    public static final String EVENTBUS_ROUTE = "/eventbus/*";

    public static final String EVENTBUS_ADDR_REGEX = "dashboard.*";

    public static final String EVENTBUS_THROUGHPUT = "dashboard.throughput";

    public static final String EVENTBUS_TREND = "dashboard.garage-trend";

    public static final String EVENTBUS_RANKING = "dashboard.ranking";

}
