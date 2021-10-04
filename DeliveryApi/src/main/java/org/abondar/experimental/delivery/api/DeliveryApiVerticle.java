package org.abondar.experimental.delivery.api;


import io.reactivex.Completable;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.client.WebClient;
import org.abondar.experimental.delivery.api.util.CertificateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.abondar.experimental.delivery.api.util.ApiUtil.API_PORT;
import static org.abondar.experimental.delivery.api.util.ApiUtil.API_PREFIX;
import static org.abondar.experimental.delivery.api.util.ApiUtil.DAY_PARAM;
import static org.abondar.experimental.delivery.api.util.ApiUtil.MONTH_PARAM;
import static org.abondar.experimental.delivery.api.util.ApiUtil.PARAM_DELIM;
import static org.abondar.experimental.delivery.api.util.ApiUtil.REGISTER_ENDPOINT;
import static org.abondar.experimental.delivery.api.util.ApiUtil.TOKEN_ENDPOINT;
import static org.abondar.experimental.delivery.api.util.ApiUtil.USERNAME_PARAM;
import static org.abondar.experimental.delivery.api.util.ApiUtil.USER_TOTAL_ENDPOINT;
import static org.abondar.experimental.delivery.api.util.ApiUtil.YEAR_PARAM;


public class DeliveryApiVerticle extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(DeliveryApiVerticle.class);


    @Override
    public Completable rxStart() {

        return vertx.createHttpServer()
                .requestHandler(buildRouter())
                .rxListen(API_PORT)
                .ignoreElement();

    }


    private Router buildRouter() {
        var router = Router.router(vertx);

        var auth = buildAuth();
        var webClient = WebClient.create(vertx);
        var handler = new Handler();

        router.route().handler(handler.corsHandler());
        router.post().handler(handler.bodyHandler());
        router.put().handler(handler.bodyHandler());

        router.post(API_PREFIX + REGISTER_ENDPOINT)
                .handler(rc -> handler.registerHandler(rc, webClient));

        router.post(API_PREFIX+TOKEN_ENDPOINT)
                .handler(rc -> handler.tokenHandler(rc,webClient,auth));

        var jwtHandler = handler.jwtHandler(auth);
        router.get(API_PREFIX+ PARAM_DELIM +USERNAME_PARAM)
                .handler(jwtHandler)
                .handler(handler::checkUserHandler)
                .handler(rc-> handler.fetchUserHandler(rc,webClient));

        router.put(API_PREFIX+ PARAM_DELIM +USERNAME_PARAM)
                .handler(jwtHandler)
                .handler(handler::checkUserHandler)
                .handler(rc-> handler.updateUserHandler(rc,webClient));

        router.get(API_PREFIX+ PARAM_DELIM +USERNAME_PARAM+USER_TOTAL_ENDPOINT)
                .handler(jwtHandler)
                .handler(handler::checkUserHandler)
                .handler(rc-> handler.totalKilometersHandler(rc,webClient));

        router.get(API_PREFIX+ PARAM_DELIM +USERNAME_PARAM+
                        PARAM_DELIM +YEAR_PARAM+ PARAM_DELIM +MONTH_PARAM)
                .handler(jwtHandler)
                .handler(handler::checkUserHandler)
                .handler(rc-> handler.monthKilometersHandler(rc,webClient));

        router.get(API_PREFIX+ PARAM_DELIM +USERNAME_PARAM+
                        PARAM_DELIM +YEAR_PARAM + PARAM_DELIM +MONTH_PARAM+ PARAM_DELIM +DAY_PARAM)
                .handler(jwtHandler)
                .handler(handler::checkUserHandler)
                .handler(rc-> handler.dayKilometersHandler(rc,webClient));

        return router;
    }


    private JWTAuth buildAuth() {
        try {
            String publicKay = CertificateUtil.publicKey();
            String privateKey = CertificateUtil.privateKey();

            var authOpts = new JWTAuthOptions();
            authOpts.addPubSecKey(new PubSecKeyOptions()
                    .setAlgorithm("RS256")
                    .setBuffer(publicKay));

            authOpts.addPubSecKey(new PubSecKeyOptions()
                    .setAlgorithm("RS256")
                    .setBuffer(privateKey));


            return JWTAuth.create(vertx, authOpts);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            Completable.error(ex)
                    .blockingAwait();
        }
        return null;
    }


}
