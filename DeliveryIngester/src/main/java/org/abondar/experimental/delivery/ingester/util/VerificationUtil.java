package org.abondar.experimental.delivery.ingester.util;


import io.vertx.core.json.JsonObject;

public class VerificationUtil {

    public static boolean isInvalidPayload(JsonObject payload){
         return !payload.containsKey(IngesterUtil.DEVICE_ID_FIELD) ||
         !payload.containsKey(IngesterUtil.DEVICE_SYNC_FIELD) ||
         !payload.containsKey(IngesterUtil.DELIVERED_DAILY_FIELD) ||
         !payload.containsKey(IngesterUtil.DISTANCE_FIELD)  ||
         !payload.containsKey(IngesterUtil.CURRENT_DELIVERY_FIELD) ||
         !payload.containsKey(IngesterUtil.CURRENT_DESCRIPTION_FIELD);
    }

}
