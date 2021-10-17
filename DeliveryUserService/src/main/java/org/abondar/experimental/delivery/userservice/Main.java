package org.abondar.experimental.delivery.userservice;

import io.vertx.reactivex.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {
        var vertx = Vertx.vertx();

        vertx.rxDeployVerticle(new UserServiceVerticle())
                .subscribe(
                        ok -> logger.info("Server up on port 5000"),
                        err -> logger.info("Error: ",err)
                );
    }
}
