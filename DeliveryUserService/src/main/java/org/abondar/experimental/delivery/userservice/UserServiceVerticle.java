package org.abondar.experimental.delivery.userservice;

import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import org.abondar.experimental.delivery.userservice.service.MongoServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.abondar.experimental.delivery.userservice.util.ApiUtil.AUTH_ENDPOINT;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.DEVICE_ENDPOINT;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.DEVICE_PARAM;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.REGISTER_ENDPOINT;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.SERVER_PORT;
import static org.abondar.experimental.delivery.userservice.util.ApiUtil.USERNAME_PARAM;

public class UserServiceVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceVerticle.class);


    @Override
    public Completable rxStart() {
        var router = Router.router(vertx);

        var mongoService = new MongoServiceImpl(vertx);
        var handler = new ApiHandler(mongoService);

        router.post()
                .handler(handler.bodyHandler());
        router.put()
                .handler(handler.bodyHandler());
        router.post(REGISTER_ENDPOINT)
                .handler(handler::validateRegistration)
                .handler(handler::register);
        router.get(USERNAME_PARAM).handler(handler::fetchUser);
        router.put(USERNAME_PARAM).handler(handler::updateUser);
        router.post(AUTH_ENDPOINT).handler(handler::authenticate);
        router.get(DEVICE_ENDPOINT+DEVICE_PARAM).handler(handler::fetchDevice);


        return vertx.createHttpServer()
                .requestHandler(router)
                .rxListen(SERVER_PORT)
                .ignoreElement();
    }
}
