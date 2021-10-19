package org.abondar.experimental.delivery.userservice.service;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public interface MongoService {

    Completable registerUser(String username, String password, JsonObject extraInfo);

    Single<JsonObject> getUser(String username);

    Completable updateUser(String username, JsonObject body);

    Single<JsonObject> getDevice(String deviceId);
}
