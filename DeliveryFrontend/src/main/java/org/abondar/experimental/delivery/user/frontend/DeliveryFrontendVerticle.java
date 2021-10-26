package org.abondar.experimental.delivery.user.frontend;

import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeliveryFrontendVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryFrontendVerticle.class);

    private static final int SERVER_PORT = 8080;

    @Override
    public Completable rxStart(){
        var router = Router.router(vertx);

        router.route()
                .handler(StaticHandler.create("webroot/assets"));
        router.get("/*")
                .handler(ctx -> ctx.reroute("/index.html"));

        return vertx.createHttpServer()
                .requestHandler(router)
                .rxListen(SERVER_PORT)
                .ignoreElement();

    }
}
