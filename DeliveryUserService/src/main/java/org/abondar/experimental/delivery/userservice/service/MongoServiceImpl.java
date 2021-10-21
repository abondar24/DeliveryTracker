package org.abondar.experimental.delivery.userservice.service;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.mongo.MongoAuthenticationOptions;
import io.vertx.ext.auth.mongo.MongoAuthorizationOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.auth.mongo.MongoAuthentication;
import io.vertx.reactivex.ext.auth.mongo.MongoUserUtil;
import io.vertx.reactivex.ext.mongo.MongoClient;

import static org.abondar.experimental.delivery.userservice.util.ApiUtil.DEVICE_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.EMAIL_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.GARAGE_FIELD;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.USERNAME_FIELD;
import static org.abondar.experimental.delivery.userservice.util.MongoUtil.DATABASE_NAME;
import static org.abondar.experimental.delivery.userservice.util.MongoUtil.ID;
import static org.abondar.experimental.delivery.userservice.util.MongoUtil.INDEX_ERROR;
import static org.abondar.experimental.delivery.userservice.util.MongoUtil.MONGO_HOST;
import static org.abondar.experimental.delivery.userservice.util.MongoUtil.MONGO_PASS;
import static org.abondar.experimental.delivery.userservice.util.MongoUtil.MONGO_PORT;
import static org.abondar.experimental.delivery.userservice.util.MongoUtil.MONGO_USER;
import static org.abondar.experimental.delivery.userservice.util.MongoUtil.SET;
import static org.abondar.experimental.delivery.userservice.util.MongoUtil.USER_COLLECTION;


public class MongoServiceImpl implements MongoService {

    private final MongoClient mongoClient;

    public MongoServiceImpl(Vertx vertx) {
        var config = new JsonObject();
        config.put("host", MONGO_HOST);
        config.put("port", MONGO_PORT);
        config.put("username",MONGO_USER);
        config.put("password",MONGO_PASS);

        config.put("db_name", DATABASE_NAME);

        this.mongoClient = MongoClient.createShared(vertx, config);
    }

    @Override
    public Completable registerUser(String username, String password, JsonObject extraInfo) {
        var userUtil = MongoUserUtil.create(mongoClient,
                new MongoAuthenticationOptions(), new MongoAuthorizationOptions());

        return userUtil.rxCreateUser(username, password)
                .flatMapMaybe(docId -> insertExtraInfo(extraInfo, docId))
                .ignoreElement();
    }

    private MaybeSource<? extends JsonObject> insertExtraInfo(JsonObject extraInfo, String docId) {
        var query = new JsonObject();
        query.put(ID, docId);
        return mongoClient.rxFindOneAndUpdate(USER_COLLECTION, query, extraInfo)
                .onErrorResumeNext(err -> {
                    return deleteIncompleteUser(query, err);
                });
    }

    private MaybeSource<? extends JsonObject> deleteIncompleteUser(JsonObject query, Throwable err) {
        if (isIndexViolated(err)) {
            return mongoClient.rxRemoveDocument(USER_COLLECTION, query)
                    .flatMap(del -> Maybe.error(err));
        } else {
            return Maybe.error(err);
        }

    }

    private boolean isIndexViolated(Throwable err) {
        return err.getMessage().contains(INDEX_ERROR);
    }

    @Override
    public Single<JsonObject> getUser(String username) {
        var query = new JsonObject();
        query.put(USERNAME_FIELD, username);

        var fields = new JsonObject();
        fields.put(ID, 0);
        fields.put(USERNAME_FIELD, 1);
        fields.put(EMAIL_FIELD, 1);
        fields.put(DEVICE_FIELD, 1);
        fields.put(GARAGE_FIELD, 1);

        return mongoClient.rxFindOne(USER_COLLECTION, query, fields)
                .toSingle();
    }

    @Override
    public Completable updateUser(String username, JsonObject body) {
        var query = new JsonObject();
        query.put(USERNAME_FIELD, username);

        var updates = new JsonObject();
        if (body.containsKey(GARAGE_FIELD)) {
            updates.put(GARAGE_FIELD, body.getString(GARAGE_FIELD));
        }

        if (body.containsKey(EMAIL_FIELD)) {
            updates.put(EMAIL_FIELD, body.getString(EMAIL_FIELD));
        }

        if (updates.isEmpty()) {
            return Completable.complete();
        }

        var updateSet = new JsonObject();
        updateSet.put(SET, updates);

        return mongoClient.rxFindOneAndUpdate(USER_COLLECTION, query, updateSet)
                .ignoreElement();

    }

    @Override
    public Single<JsonObject> getDevice(String deviceId) {
        var query = new JsonObject();
        query.put(DEVICE_FIELD, deviceId);

        var fields = new JsonObject();
        fields.put(ID, 0);
        fields.put(USERNAME_FIELD, 1);
        fields.put(DEVICE_FIELD, 1);

        return mongoClient.rxFindOne(USER_COLLECTION, query, fields)
                .toSingle();
    }

    public Single<User> authenticateUser(JsonObject json){
        var authProvider = MongoAuthentication.create(mongoClient,new MongoAuthenticationOptions());
        return authProvider.rxAuthenticate(json);
    }

    @Override
    public void cleanDb(String collection) {
           mongoClient.dropCollection(USER_COLLECTION);
    }
}
