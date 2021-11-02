package org.abondar.experimental.delivery.api;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.reactivex.Single;
import io.vertx.circuitbreaker.OpenCircuitException;
import io.vertx.core.TimeoutStream;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.client.predicate.ResponsePredicate;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.abondar.experimental.delivery.api.util.ApiUtil.ACTIVITY_SERVICE_PORT;
import static org.abondar.experimental.delivery.api.util.ApiUtil.AUTH_ENDPOINT;
import static org.abondar.experimental.delivery.api.util.ApiUtil.CURRENT_ENDPOINT;
import static org.abondar.experimental.delivery.api.util.ApiUtil.DAY_PARAM;
import static org.abondar.experimental.delivery.api.util.ApiUtil.DEVICE_ID_PARAM;
import static org.abondar.experimental.delivery.api.util.ApiUtil.MONTH_PARAM;
import static org.abondar.experimental.delivery.api.util.ApiUtil.PATH_DELIM;
import static org.abondar.experimental.delivery.api.util.ApiUtil.REGISTER_ENDPOINT;
import static org.abondar.experimental.delivery.api.util.ApiUtil.SERVER_HOST;
import static org.abondar.experimental.delivery.api.util.ApiUtil.USERNAME_PARAM;
import static org.abondar.experimental.delivery.api.util.ApiUtil.USER_SERVICE_PORT;
import static org.abondar.experimental.delivery.api.util.ApiUtil.USER_TOTAL_ENDPOINT;
import static org.abondar.experimental.delivery.api.util.ApiUtil.YEAR_PARAM;

public class Handler {

    private final Cache<String, CacheValue> deliveryCache;

    public Handler() {
        this.deliveryCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .build();
    }

    public BodyHandler bodyHandler() {
        return BodyHandler.create();
    }

    public CorsHandler corsHandler() {

        var httpMethods = Set.of(
                HttpMethod.POST,
                HttpMethod.PUT,
                HttpMethod.GET,
                HttpMethod.OPTIONS
        );

        var headers = Arrays.stream(Headers.values())
                .filter(h -> h != Headers.JSON)
                .filter(h -> h != Headers.JWT)
                .map(Headers::getVal)
                .collect(Collectors.toSet());

        return CorsHandler.create("*")
                .allowedHeaders(headers)
                .allowedMethods(httpMethods);
    }

    public JWTAuthHandler jwtHandler(JWTAuth auth) {
        return JWTAuthHandler.create(auth);
    }


    public void registerHandler(RoutingContext ctx, WebClient webClient) {
        webClient.post(USER_SERVICE_PORT, SERVER_HOST, REGISTER_ENDPOINT)
                .putHeader(Headers.CONTENT_TYPE.getVal(), Headers.JSON.getVal())
                .rxSendJson(ctx.getBodyAsJson())
                .subscribe(
                        response -> sendResponse(ctx, response.statusCode()),
                        error -> sendErrorResponse(ctx, 502, error)
                );
    }

    public void tokenHandler(RoutingContext ctx, WebClient webClient, JWTAuth auth, CircuitBreaker circuitBreaker) {
        circuitBreaker.<String>rxExecute(
                promise -> {
                    var payload = ctx.getBodyAsJson();
                    var username = payload.getString(USERNAME_PARAM);

                    webClient.post(USER_SERVICE_PORT, SERVER_HOST, AUTH_ENDPOINT)
                            .expect(ResponsePredicate.SC_SUCCESS)
                            .rxSendJson(ctx.getBodyAsJson())
                            .flatMap(resp -> fetchUserDetails(username, webClient))
                            .map(resp -> resp.body().getString(DEVICE_ID_PARAM))
                            .map(deviceId -> generateToken(username, deviceId, auth))
                            .subscribe(promise::complete,
                                    err -> {
                                        if (err instanceof NoStackTraceThrowable) {
                                            promise.complete("");
                                        } else {
                                            promise.fail(err);
                                        }
                                    });
                }
        ).subscribe(
                token -> sendToken(ctx, token),
                err -> sendAuthError(ctx, err)
        );


    }

    private void sendToken(RoutingContext ctx, String token) {
        if (token.isEmpty()) {
            sendAuthError(ctx, null);
        } else {
            ctx.response()
                    .putHeader(Headers.CONTENT_TYPE.getVal(), Headers.JWT.getVal())
                    .end(token);
        }

    }

    private String generateToken(String username, String deviceId, JWTAuth auth) {
        var claims = new JsonObject();
        claims.put(DEVICE_ID_PARAM, deviceId);

        var opts = new JWTOptions();
        opts.setAlgorithm("RS256");
        opts.setExpiresInMinutes(10_000);
        opts.setIssuer("DeliveryTracker");
        opts.setSubject(username);

        return auth.generateToken(claims, opts);
    }

    private Single<HttpResponse<JsonObject>> fetchUserDetails(String username, WebClient webClient) {
        return webClient.get(USER_SERVICE_PORT, SERVER_HOST, PATH_DELIM + username)
                .expect(ResponsePredicate.SC_OK)
                .as(BodyCodec.jsonObject())
                .rxSend();
    }


    public void checkUserHandler(RoutingContext ctx) {
        var subject = ctx.user()
                .principal()
                .getString("sub");

        if (!ctx.pathParam(USERNAME_PARAM).equals(subject)) {
            sendResponse(ctx, 403);
        } else {
            ctx.next();
        }
    }

    public void fetchUserHandler(RoutingContext ctx, WebClient webClient) {
        fetchUserDetails(ctx.pathParam(USERNAME_PARAM), webClient)
                .subscribe(
                        resp -> sendResponse(ctx, resp),
                        err -> sendErrorResponse(ctx, 502, err)
                );
    }

    public void updateUserHandler(RoutingContext ctx, WebClient webClient) {
        webClient.put(USER_SERVICE_PORT, SERVER_HOST, PATH_DELIM + ctx.pathParam(USERNAME_PARAM))
                .putHeader(Headers.CONTENT_TYPE.getVal(), Headers.JSON.getVal())
                .expect(ResponsePredicate.SC_OK)
                .rxSendBuffer(ctx.getBody())
                .subscribe(
                        resp -> ctx.response().end(),
                        err -> sendErrorResponse(ctx, 502, err)
                );
    }

    public void totalHandler(RoutingContext ctx, WebClient webClient, CircuitBreaker circuitBreaker) {
        var deviceId = ctx.user()
                .principal()
                .getString(DEVICE_ID_PARAM);

        circuitBreaker.<Void>executeWithFallback(promise ->
                webClient.get(ACTIVITY_SERVICE_PORT, SERVER_HOST, PATH_DELIM + deviceId + USER_TOTAL_ENDPOINT)
                .timeout(5000)
                .as(BodyCodec.jsonObject())
                .rxSend()
                .subscribe(
                        resp -> {
                            addToCache(deviceId,resp);
                            sendResponse(ctx, resp);
                            promise.complete();
                        },
                        err -> {
                            recoverFromCache(ctx,err,deviceId);

                            promise.fail(err);
                        }
                ), err -> {
            recoverFromCache(ctx,err,deviceId);
            return  null;
        });

    }

    private void addToCache(String deviceId, HttpResponse<JsonObject> response){
        var delivered  =response.body().getInteger("delivered");
        var distance = response.body().getInteger("distance");

        var cacheVal = new CacheValue(delivered,distance);
        deliveryCache.put(deviceId,cacheVal);
    }

    private void recoverFromCache(RoutingContext ctx,Throwable err,String deviceId){
         var val = deliveryCache.getIfPresent(deviceId);
         if (val==null){
             sendErrorResponse(ctx, 502, err);
         } else {
             var payload = new JsonObject();
             payload.put("delivered",val.delivered);
             payload.put("distance",val.distance);

             ctx.response()
                     .putHeader(Headers.CONTENT_TYPE.getVal(), Headers.JSON.getVal())
                     .end(payload
                             .encode());
         }
    }

    public void monthHandler(RoutingContext ctx, WebClient webClient) {
        var deviceId = ctx.user()
                .principal()
                .getString(DEVICE_ID_PARAM);

        var year = ctx.pathParam(YEAR_PARAM);
        var moth = ctx.pathParam(MONTH_PARAM);

        webClient.get(ACTIVITY_SERVICE_PORT, SERVER_HOST,
                        PATH_DELIM + deviceId + PATH_DELIM + year + PATH_DELIM + moth)
                .as(BodyCodec.jsonObject())
                .rxSend()
                .subscribe(
                        resp -> sendResponse(ctx, resp),
                        err -> sendErrorResponse(ctx, 502, err)
                );
    }

    public void dayHandler(RoutingContext ctx, WebClient webClient) {
        var deviceId = ctx.user()
                .principal()
                .getString(DEVICE_ID_PARAM);

        var year = ctx.pathParam(YEAR_PARAM);
        var moth = ctx.pathParam(MONTH_PARAM);
        var day = ctx.pathParam(DAY_PARAM);

        webClient.get(ACTIVITY_SERVICE_PORT, SERVER_HOST,
                        PATH_DELIM + deviceId + PATH_DELIM + year + PATH_DELIM + moth + PATH_DELIM + day)
                .as(BodyCodec.jsonObject())
                .rxSend()
                .subscribe(
                        resp -> sendResponse(ctx, resp),
                        err -> sendErrorResponse(ctx, 502, err)
                );
    }

    public void currentDeliveryHandler(RoutingContext ctx, WebClient webClient) {
        var deviceId = ctx.user()
                .principal()
                .getString(DEVICE_ID_PARAM);

        webClient.get(ACTIVITY_SERVICE_PORT, SERVER_HOST,
                        PATH_DELIM + deviceId + CURRENT_ENDPOINT)
                .as(BodyCodec.jsonObject())
                .rxSend()
                .subscribe(
                        resp -> sendResponse(ctx, resp),
                        err -> sendErrorResponse(ctx, 502, err)
                );
    }

    private void sendResponse(RoutingContext ctx, HttpResponse<JsonObject> resp) {
        if (resp.statusCode() != 200) {
            sendResponse(ctx, resp.statusCode());
        } else {
            ctx.response()
                    .putHeader(Headers.CONTENT_TYPE.getVal(), Headers.JSON.getVal())
                    .end(resp.body()
                            .encode());
        }
    }

    private void sendResponse(RoutingContext ctx, int code) {
        ctx.response()
                .setStatusCode(code)
                .end();
    }

    private void sendErrorResponse(RoutingContext ctx, int code, Throwable err) {
        ctx.fail(code, err);
    }


    private void sendAuthError(RoutingContext ctx, Throwable err) {
        if (err instanceof OpenCircuitException) {
            ctx.fail(504);
        } else if (err instanceof TimeoutStream) {
            ctx.fail(504);
        } else {
            ctx.fail(401);
        }
    }

    private static class CacheValue {
        private final int delivered;
        private final int distance;

        public CacheValue(int delivered, int distance) {
            this.delivered = delivered;
            this.distance = distance;
        }


    }
}


