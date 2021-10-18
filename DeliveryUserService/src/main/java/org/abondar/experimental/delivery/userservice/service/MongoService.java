package org.abondar.experimental.delivery.userservice.service;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;

public interface MongoService {

    void registerUser(String username, String password, JsonObject extraInfo);

    void getUser(String username);

    void updateUser(String username,JsonObject body);

    void getDevice(String deviceId);
}
