package org.abondar.experimental.delivery.api;


import io.vertx.reactivex.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.abondar.experimental.delivery.api.util.ApiUtil.API_PORT;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        var vertx = Vertx.vertx();

        vertx.rxDeployVerticle(new DeliveryApiVerticle())
                .subscribe(
                        ok -> logger.info("Server up on port {}",API_PORT),
                        err -> logger.info("Error: ",err)
                );
    }
}
