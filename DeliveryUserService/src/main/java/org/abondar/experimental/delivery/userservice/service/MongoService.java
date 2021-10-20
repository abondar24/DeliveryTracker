package org.abondar.experimental.delivery.userservice.service;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.auth.User;

public interface MongoService {

    Completable registerUser(String username, String password, JsonObject extraInfo);

    Single<JsonObject> getUser(String username);

    Completable updateUser(String username, JsonObject body);

    Single<JsonObject> getDevice(String deviceId);

    Single<User> authenticateUser(JsonObject json);

    void cleanDb(String collection);
}
