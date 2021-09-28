package org.abondar.experimental.delivery.api;


import io.reactivex.Completable;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.client.WebClient;
import org.abondar.experimental.delivery.api.util.CertificateUtil;
import org.abondar.experimental.delivery.api.util.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class DeliveryApiVerticle extends AbstractVerticle {

    private  final Logger logger = LoggerFactory.getLogger(DeliveryApiVerticle.class);

    private final int HTTP_PORT = 8000;

    @Override
    public Completable rxStart() {

        return vertx.createHttpServer()
                .requestHandler(buildRouter())
                .rxListen(HTTP_PORT)
                .ignoreElement();

    }


    private Router buildRouter(){
        var router = Router.router(vertx);

        var auth = buildAuth();
        var webClient = WebClient.create(vertx);

        router.route().handler(HandlerUtil.corsHandler());
        router.post().handler(HandlerUtil.bodyHandler());
        router.put().handler(HandlerUtil.bodyHandler());

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


            return JWTAuth.create(vertx,authOpts);
        } catch (IOException ex){
            logger.error(ex.getMessage());
            Completable.error(ex)
                    .blockingAwait();
        }
        return null;
    }


}
