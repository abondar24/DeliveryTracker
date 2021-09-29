package org.abondar.experimental.delivery.user.frontend;

import io.vertx.reactivex.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        var vertx = Vertx.vertx();

        vertx.rxDeployVerticle(new UserFrontendVerticle())
                .subscribe(
                        ok -> logger.info("Server up on port 8080"),
                        err -> logger.info("Error: ",err)
                );
    }
}
