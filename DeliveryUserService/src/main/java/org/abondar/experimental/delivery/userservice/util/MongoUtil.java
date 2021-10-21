package org.abondar.experimental.delivery.userservice.util;

public class MongoUtil {
    public static final String MONGO_HOST = "localhost";

    public static final int MONGO_PORT = 27017;

    public static final String MONGO_USER = "admin";

    public static final String MONGO_PASS = "admin123";

    public static final String DATABASE_NAME = "profiles";

    public static final String USER_COLLECTION = "user";

    public static final String ID = "_id";

    public static final String SET = "$set";

    public static final String INDEX_ERROR = "E11000";

    private MongoUtil() {
    }
}
