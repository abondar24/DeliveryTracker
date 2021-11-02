package org.abondar.experimental.delivery.api;


import io.reactivex.Completable;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.client.WebClient;
import org.abondar.experimental.delivery.api.util.CertificateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.abondar.experimental.delivery.api.util.ApiUtil.ACTIVITY_CIRCUIT_BREAKER;
import static org.abondar.experimental.delivery.api.util.ApiUtil.API_PORT;
import static org.abondar.experimental.delivery.api.util.ApiUtil.CIRCUIT_BREAKER_CLOSED;
import static org.abondar.experimental.delivery.api.util.ApiUtil.CIRCUIT_BREAKER_HALF_OPEN;
import static org.abondar.experimental.delivery.api.util.ApiUtil.CIRCUIT_BREAKER_OPEN;
import static org.abondar.experimental.delivery.api.util.ApiUtil.CURRENT_ENDPOINT;
import static org.abondar.experimental.delivery.api.util.ApiUtil.DAY_ENDPOINT;
import static org.abondar.experimental.delivery.api.util.ApiUtil.MONTH_ENDPOINT;
import static org.abondar.experimental.delivery.api.util.ApiUtil.REGISTER_ENDPOINT;
import static org.abondar.experimental.delivery.api.util.ApiUtil.TOKEN_CIRCUIT_BREAKER;
import static org.abondar.experimental.delivery.api.util.ApiUtil.TOKEN_ENDPOINT;
import static org.abondar.experimental.delivery.api.util.ApiUtil.USER_ENDPOINT;
import static org.abondar.experimental.delivery.api.util.ApiUtil.USER_TOTAL_ENDPOINT;


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

        var cbOpts = getCircuitBreakerOptions();

        var tokenCircuitBreaker = CircuitBreaker.create(TOKEN_CIRCUIT_BREAKER,vertx,cbOpts);
        tokenCircuitBreaker.openHandler(v->this.logCircuitBreaker(CIRCUIT_BREAKER_OPEN,TOKEN_CIRCUIT_BREAKER));
        tokenCircuitBreaker.halfOpenHandler(v->this.logCircuitBreaker(CIRCUIT_BREAKER_HALF_OPEN,TOKEN_CIRCUIT_BREAKER));
        tokenCircuitBreaker.closeHandler(v->this.logCircuitBreaker(CIRCUIT_BREAKER_CLOSED,TOKEN_CIRCUIT_BREAKER));

        var activityCircuitBreaker = CircuitBreaker.create(ACTIVITY_CIRCUIT_BREAKER,vertx,cbOpts);
        activityCircuitBreaker.openHandler(v->this.logCircuitBreaker(CIRCUIT_BREAKER_OPEN,ACTIVITY_CIRCUIT_BREAKER));
        activityCircuitBreaker.halfOpenHandler(v->this.logCircuitBreaker(CIRCUIT_BREAKER_HALF_OPEN,ACTIVITY_CIRCUIT_BREAKER));
        activityCircuitBreaker.closeHandler(v->this.logCircuitBreaker(CIRCUIT_BREAKER_CLOSED,ACTIVITY_CIRCUIT_BREAKER));

        router.route().handler(handler.corsHandler());
        router.post().handler(handler.bodyHandler());
        router.put().handler(handler.bodyHandler());

        router.post(REGISTER_ENDPOINT)
                .handler(rc -> handler.registerHandler(rc, webClient));

        router.post(TOKEN_ENDPOINT)
                .handler(rc -> handler.tokenHandler(rc, webClient, auth,tokenCircuitBreaker));

        var jwtHandler = handler.jwtHandler(auth);
        router.get(USER_ENDPOINT)
                .handler(jwtHandler)
                .handler(handler::checkUserHandler)
                .handler(rc -> handler.fetchUserHandler(rc, webClient));

        router.put(USER_ENDPOINT)
                .handler(jwtHandler)
                .handler(handler::checkUserHandler)
                .handler(rc -> handler.updateUserHandler(rc, webClient));

        router.get(USER_TOTAL_ENDPOINT)
                .handler(jwtHandler)
                .handler(handler::checkUserHandler)
                .handler(rc -> handler.totalHandler(rc, webClient,activityCircuitBreaker));

        router.get(MONTH_ENDPOINT)
                .handler(jwtHandler)
                .handler(handler::checkUserHandler)
                .handler(rc -> handler.monthHandler(rc, webClient));

        router.get(DAY_ENDPOINT)
                .handler(jwtHandler)
                .handler(handler::checkUserHandler)
                .handler(rc -> handler.dayHandler(rc, webClient));

        router.get(CURRENT_ENDPOINT)
                .handler(jwtHandler)
                .handler(handler::checkUserHandler)
                .handler(rc -> handler.currentDeliveryHandler(rc, webClient));

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

    private CircuitBreakerOptions getCircuitBreakerOptions(){
        var opts = new CircuitBreakerOptions();
        opts.setMaxFailures(5);
        opts.setMaxRetries(0);
        opts.setTimeout(5000);//5 seconds
        opts.setResetTimeout(10_000);

        return opts;
    }

    private void logCircuitBreaker(String state, String cbName){
        logger.info("Circuit breaker {} is now {}",cbName,state);
    }

}
