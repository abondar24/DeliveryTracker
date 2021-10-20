package org.abondar.experimental.userservice;

import com.sun.security.auth.UnixNumericUserPrincipal;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.auth.User;
import org.abondar.experimental.delivery.userservice.service.MongoService;

import java.util.NoSuchElementException;

import static org.abondar.experimental.delivery.userservice.util.ApiUtil.PASSWORD_FIELD;
import static org.abondar.experimental.delivery.userservice.util.MongoUtil.INDEX_ERROR;

public class MongoServiceTestImpl implements MongoService {
    @Override
    public Completable registerUser(String username, String password, JsonObject extraInfo) {
        if (username.equals("errUser")){
            return Completable.error(new RuntimeException(INDEX_ERROR));
        }

        if (username.equals("err")){
            return Completable.error(new RuntimeException(""));
        }

        return Completable.complete();
    }

    @Override
    public Single<JsonObject> getUser(String username) {
        if (username.equals("errUser")){
            return Single.error(new NoSuchFieldException());
        }
        return Single.just(new JsonObject());
    }

    @Override
    public Completable updateUser(String username, JsonObject body) {
        return Completable.complete();
    }

    @Override
    public Single<JsonObject> getDevice(String deviceId) {
        return Single.just(new JsonObject());
    }

    @Override
    public Single<User> authenticateUser(JsonObject json) {
        var pwd = json.getString(PASSWORD_FIELD);
        if (!pwd.equals("test")){
           return Single.error(new RuntimeException());
        }
        return Single.just(User.create(json));
    }
}
