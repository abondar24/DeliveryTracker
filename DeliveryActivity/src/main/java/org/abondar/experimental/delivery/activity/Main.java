package org.abondar.experimental.delivery.activity;

import io.vertx.reactivex.core.Vertx;
import org.abondar.experimental.delivery.activity.verticle.ActivityApiVerticle;
import org.abondar.experimental.delivery.activity.verticle.EventVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.abondar.experimental.delivery.activity.util.ActivityUtil.SERVER_PORT;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.setProperty("vertx.logger-delegate-factory-class-name",
                "io.vertx.core.logging.SLF4JLogDelegateFactory");

        var vertx = Vertx.vertx();
        vertx.rxDeployVerticle(new EventVerticle())
                .flatMap(id->vertx.rxDeployVerticle(new ActivityApiVerticle()))
                .subscribe(
                        ok-> logger.info("Server running on port {}",SERVER_PORT),
                        err -> logger.error("Server error",err)
                );
    }
}
