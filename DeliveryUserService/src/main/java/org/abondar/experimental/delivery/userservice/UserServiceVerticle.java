package org.abondar.experimental.delivery.userservice;

import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.abondar.experimental.delivery.userservice.util.ApiUtil.SERVER_PORT;

public class UserServiceVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceVerticle.class);

    @Override
    public Completable rxStart(){
        var router = Router.router(vertx);
        return  vertx.createHttpServer()
                .requestHandler(router)
                .rxListen(SERVER_PORT)
                .ignoreElement();
    }
}
