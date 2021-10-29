package org.abondar.experimental.delivery.activity.util;

import io.vertx.pgclient.PgConnectOptions;

public class DbUtil {

    private DbUtil(){}

    private static final String DATABASE_HOST = "localhost";

    private static final String DATABASE = "delivery";

    private static final String DATABASE_USER = "admin";

    private static final String DATABASE_PASSWORD = "admin217";

    public static final PgConnectOptions pgConnectOptions = new PgConnectOptions()
            .setHost(DATABASE_HOST)
            .setDatabase(DATABASE)
            .setUser(DATABASE_USER)
            .setPassword(DATABASE_PASSWORD);

    public static final String DEVICE_ID_COLUMN = "device_id";

    public static final String DEVICE_SYNC_COLUMN = "device_sync";

    public static final String SYNC_TIMESTAMP_COLUMN = "sync_timestamptz";

    public static final String DISTANCE_COLUMN = "distance";

    public static final String DESCRIPTION_COLUMN = "desription";
}
