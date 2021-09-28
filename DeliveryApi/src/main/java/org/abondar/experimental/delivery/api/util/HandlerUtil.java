package org.abondar.experimental.delivery.api.util;

import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import org.abondar.experimental.delivery.api.Headers;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class HandlerUtil {


    private HandlerUtil() {
    }

    public static BodyHandler bodyHandler() {
        return BodyHandler.create();
    }

    public static CorsHandler corsHandler() {

        var httpMethods = Set.of(
                HttpMethod.POST,
                HttpMethod.PUT,
                HttpMethod.GET,
                HttpMethod.OPTIONS
        );

        var headers = Arrays.stream(Headers.values())
                .map(Headers::getVal)
                .collect(Collectors.toSet());

        return CorsHandler.create("*")
                .allowedHeaders(headers)
                .allowedMethods(httpMethods);
    }

    public static JWTAuthHandler jwtHandler(JWTAuth auth) {
        return JWTAuthHandler.create(auth);
    }
}
