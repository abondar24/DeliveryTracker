package org.abondar.experimental.delivery.userservice;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import org.abondar.experimental.delivery.userservice.service.AuthService;
import org.abondar.experimental.delivery.userservice.service.MongoService;

import java.util.regex.Pattern;

import static org.abondar.experimental.delivery.userservice.util.ApiUtil.DEVICE_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.EMAIL_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.GARAGE_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.PASSWORD_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.USERNAME_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.USERNAME_PARAM;

public class ApiHandler {

    private MongoService mongoService;

    private AuthService authService;

    public ApiHandler(MongoService mongoService,AuthService authService){
        this.mongoService = mongoService;
        this.authService =  authService;
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
        extraInfo.put("$set",new JsonObject());
        extraInfo.put(EMAIL_FIELD,body.getString(EMAIL_FIELD));
        extraInfo.put(GARAGE_FIELD,body.getString(GARAGE_FIELD));
        extraInfo.put(DEVICE_FIELD,body.getString(DEVICE_FIELD));

        mongoService.registerUser(username,password,extraInfo);
    }

    public void fetchUser(RoutingContext rc){
        var username = rc.pathParam(USERNAME_FIELD);
        mongoService.getUser(username);
    }

    public void updateUser(RoutingContext rc){
        var username = rc.pathParam(USERNAME_FIELD);
        var body = getBody(rc);

        mongoService.updateUser(username,body);
    }

    public void authenticate(RoutingContext rc){
        var body = getBody(rc);
        authService.authenticateUser(body);
    }

    public void fetchDevice(RoutingContext rc){
        var deviceId = rc.pathParam(DEVICE_FIELD);
        mongoService.getDevice(deviceId);
    }

    private boolean isFieldMissing(JsonObject body) {
        return !(body.containsKey(USERNAME_FIELD) &&
                body.containsKey(PASSWORD_FIELD) &&
                body.containsKey(EMAIL_FIELD) &&
                body.containsKey(GARAGE_FIELD) &&
                body.containsKey(DEVICE_FIELD));
    }

    private boolean isFieldWrong(JsonObject body){
        var usernamePattern = Pattern.compile("\\w[\\w+]-]*");
        var deviceIdPattern = Pattern.compile("\\w[\\w+]-]*");
        var emailPattern =  Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

        return !usernamePattern.matcher(body.getString(USERNAME_FIELD)).matches() ||
                !emailPattern.matcher(body.getString(EMAIL_FIELD)).matches() ||
                body.getString(PASSWORD_FIELD).trim().isEmpty() ||
                !deviceIdPattern.matcher(body.getString(DEVICE_FIELD)).matches();
    }


    private JsonObject getBody(RoutingContext rc){
        if (rc.getBody().length()==0){
            return new JsonObject();
        } else {
            return  rc.getBodyAsJson();
        }
    }
}
