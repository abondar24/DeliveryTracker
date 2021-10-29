package org.abondar.experimental.delivery.api.util;

public class ApiUtil {

    public static final String SERVER_HOST = "localhost";

    public static final String USERNAME_PARAM = "username";
    public static final String YEAR_PARAM = "year";
    public static final String MONTH_PARAM = "month";
    public static final String DAY_PARAM = "day";
    public static final String DEVICE_ID_PARAM = "deviceId";
    public static final String PARAM_DELIM = "/:";
    public static final String PATH_DELIM = "/";
    public static final String AUTH_ENDPOINT = "/authenticate";

    public static final int USER_SERVICE_PORT = 5000;
    public static final int ACTIVITY_SERVICE_PORT = 6000;
    public static final int API_PORT = 8000;
    public static final String API_PREFIX = "/api/v1";
    public static final String REGISTER_ENDPOINT = API_PREFIX + "/register";
    public static final String TOKEN_ENDPOINT = API_PREFIX + "/token";
    public static final String USER_ENDPOINT = API_PREFIX + PARAM_DELIM + USERNAME_PARAM;
    public static final String USER_TOTAL_ENDPOINT = USER_ENDPOINT + "/total";

    public static final String MONTH_ENDPOINT = USER_ENDPOINT + PARAM_DELIM + YEAR_PARAM + PARAM_DELIM + MONTH_PARAM;

    public static final String DAY_ENDPOINT = MONTH_ENDPOINT + PARAM_DELIM + DAY_PARAM;

    public static final String CURRENT_ENDPOINT = USER_ENDPOINT + "/current";

}
