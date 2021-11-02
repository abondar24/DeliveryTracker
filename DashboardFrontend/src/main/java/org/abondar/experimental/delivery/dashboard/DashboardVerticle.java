package org.abondar.experimental.delivery.dashboard;

import io.reactivex.Completable;

import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.StaticHandler;

import static org.abondar.experimental.delivery.dashboard.util.DashboardUtil.SERVER_PORT;

public class DashboardVerticle  extends AbstractVerticle {

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
