package org.abondar.experimental.delivery.userservice.service;

import io.vertx.core.json.JsonObject;

public interface AuthService {

    void authenticateUser(JsonObject body);
}
