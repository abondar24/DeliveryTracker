package org.abondar.experimental.delivery.stat;

import io.vertx.reactivex.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        Vertx vertx = Vertx.vertx();
        vertx.rxDeployVerticle(new StatVerticle())
                .subscribe(
                        ok -> logger.info("Service started"),
                        err -> logger.error("Error: ", err));
    }
}

