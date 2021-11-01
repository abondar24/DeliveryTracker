package org.abondar.experimental.delivery.activity.verticle;


import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.abondar.experimental.delivery.activity.service.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DateTimeException;

import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.CONTENT_TYPE_HEADER;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DAY_PARAM;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DEVICE_ID_FIELD;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.JSON_TYPE;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.MONTH_PARAM;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.YEAR_PARAM;

public class ApiHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiHandler.class);

    private final DatabaseService databaseService;

    public ApiHandler(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public void handleTotal(RoutingContext rc) {
        var deviceId = rc.pathParam(DEVICE_ID_FIELD);

        databaseService.getTotalDeliveries(deviceId)
                .subscribe(payload -> sendPayload(rc, payload),
                        err -> sendServerError(rc, err));
    }

    public void handleMonth(RoutingContext rc){
        try {
            var deviceId = rc.pathParam(DEVICE_ID_FIELD);
            var year = rc.pathParam(YEAR_PARAM);
            var month = rc.pathParam(MONTH_PARAM);

            databaseService.getMonthDeliveries(deviceId,year,month)
                    .subscribe(payload -> sendPayload(rc, payload),
                            err -> sendServerError(rc, err));
        } catch (DateTimeException | NumberFormatException ex){
                 logger.error("Error: ",ex);
                 sendBadRequest(rc);
        }

    }

    public void handleDay(RoutingContext rc){
        try {
            var deviceId = rc.pathParam(DEVICE_ID_FIELD);
            var year = rc.pathParam(YEAR_PARAM);
            var month = rc.pathParam(MONTH_PARAM);
            var day  = rc.pathParam(DAY_PARAM);

            databaseService.getDayDeliveries(deviceId,year,month,day)
                    .subscribe(payload -> sendPayload(rc, payload),
                            err -> sendServerError(rc, err));
        } catch (DateTimeException | NumberFormatException ex){
            logger.error("Error: ",ex);
            sendBadRequest(rc);
        }

    }

    public void handleRanking(RoutingContext rc){
        databaseService.getDistanceRanking().subscribe(
                payload-> sendArrayPayload(rc,payload),
                err -> sendServerError(rc,err)
        );
    }


    private void sendPayload(RoutingContext rc, JsonObject payload) {
        if (payload != null) {
            rc.response()
                    .putHeader(CONTENT_TYPE_HEADER, JSON_TYPE)
                    .end(payload.encode());
        } else {
            sendNotFound(rc);
        }
    }

    private void sendArrayPayload(RoutingContext rc, JsonArray payload) {
        if (payload != null) {
            rc.response()
                    .putHeader(CONTENT_TYPE_HEADER, JSON_TYPE)
                    .end(payload.encode());
        } else {
            sendNotFound(rc);
        }
    }

    private void sendBadRequest(RoutingContext rc){
        rc.response()
                .setStatusCode(400)
                .end();
    }

    private void sendNotFound(RoutingContext rc) {
        rc.response()
                .setStatusCode(404)
                .end();
    }

    private void sendServerError(RoutingContext rc, Throwable err) {
        logger.error("Error: ", err);
        rc.response()
                .setStatusCode(500)
                .end();
    }
}
