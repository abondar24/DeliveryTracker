package org.abondar.experimental.delivery.userservice;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import org.abondar.experimental.delivery.userservice.service.MongoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

import static org.abondar.experimental.delivery.userservice.util.ApiUtil.DEVICE_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.EMAIL_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.GARAGE_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.PASSWORD_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.USERNAME_FIELD;
import static org.abondar.experimental.delivery.userservice.util.MongoUtil.INDEX_ERROR;

public class ApiHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiHandler.class);

    private final MongoService mongoService;


    public ApiHandler(MongoService mongoService) {
        this.mongoService = mongoService;
    }

    public BodyHandler bodyHandler() {
        return BodyHandler.create();
    }

    public void validateRegistration(RoutingContext rc) {
        var body = getBody(rc);
        if (isFieldMissing(body) || isFieldWrong(body)) {
            rc.fail(400);
        } else {
            rc.next();
        }
    }


    public void register(RoutingContext rc) {
        var body = getBody(rc);
        var username = body.getString(USERNAME_FIELD);
        var password = body.getString(PASSWORD_FIELD);

        var extraInfo = new JsonObject();
        extraInfo.put("$set", new JsonObject());
        extraInfo.put(EMAIL_FIELD, body.getString(EMAIL_FIELD));
        extraInfo.put(GARAGE_FIELD, body.getString(GARAGE_FIELD));
        extraInfo.put(DEVICE_FIELD, body.getString(DEVICE_FIELD));

        mongoService.registerUser(username, password, extraInfo)
                .subscribe(
                        () -> completeRegistration(rc),
                        err -> handleRegistrationError(rc, err)
                );
    }

    public void fetchUser(RoutingContext rc) {
        var username = rc.pathParam(USERNAME_FIELD);
        mongoService.getUser(username)
                .subscribe(
                        json -> completeFetch(rc, json),
                        err -> handleFetchError(rc, err)
                );
    }

    public void updateUser(RoutingContext rc) {
        var username = rc.pathParam(USERNAME_FIELD);
        var body = getBody(rc);

        mongoService.updateUser(username, body)
                .subscribe(
                        () -> completeSuccess(rc),
                        err -> handleServerError(rc, err)
                );
    }

    public void fetchDevice(RoutingContext rc) {
        var deviceId = rc.pathParam(DEVICE_FIELD);
        mongoService.getDevice(deviceId)
                .subscribe(
                        json -> completeFetch(rc, json),
                        err -> handleFetchError(rc, err)
                );
    }

    public void authenticate(RoutingContext rc) {
        var body = getBody(rc);
        mongoService.authenticateUser(body)
                .subscribe(
                        user -> completeSuccess(rc),
                        err -> handleAuthenticationError(rc, err)
                );
    }


    private boolean isFieldMissing(JsonObject body) {
        return !(body.containsKey(USERNAME_FIELD) &&
                body.containsKey(PASSWORD_FIELD) &&
                body.containsKey(EMAIL_FIELD) &&
                body.containsKey(GARAGE_FIELD) &&
                body.containsKey(DEVICE_FIELD));
    }

    private boolean isFieldWrong(JsonObject body) {
        var usernamePattern = Pattern.compile("\\w[\\w+|-]*");
        var deviceIdPattern = Pattern.compile("\\w[\\w+|-]*");
        var emailPattern = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

        return !usernamePattern.matcher(body.getString(USERNAME_FIELD)).matches() ||
                !emailPattern.matcher(body.getString(EMAIL_FIELD)).matches() ||
                body.getString(PASSWORD_FIELD).trim().isEmpty() ||
                !deviceIdPattern.matcher(body.getString(DEVICE_FIELD)).matches();
    }


    private JsonObject getBody(RoutingContext rc) {
        if (rc.getBody().length() == 0) {
            return new JsonObject();
        } else {
            return rc.getBodyAsJson();
        }
    }

    private void completeRegistration(RoutingContext rc) {
        rc.response().end();
    }

    private void completeFetch(RoutingContext rc, JsonObject json) {
        rc.response()
                .putHeader("Content-Type", "application/json")
                .end(json.encode());
    }

    private void completeSuccess(RoutingContext rc) {
        rc.response()
                .setStatusCode(200)
                .end();
    }

    private void handleAuthenticationError(RoutingContext rc, Throwable err) {
        logger.error("Authentication error {}", err.getMessage());
        rc.response()
                .setStatusCode(401)
                .end();
    }

    private void handleFetchError(RoutingContext rc, Throwable err) {
        if (err instanceof NoSuchFieldException) {
            rc.fail(404);
        } else {
            handleServerError(rc, err);
        }
    }

    private void handleRegistrationError(RoutingContext rc, Throwable err) {
        if (err.getMessage().contains(INDEX_ERROR)) {
            logger.error("Registration failure {}", err.getMessage());
            rc.fail(409);
        } else {
            handleServerError(rc, err);
        }
    }

    private void handleServerError(RoutingContext rc, Throwable err) {
        logger.error("Server err {}", err.getMessage());
        rc.fail(500);
    }

}
