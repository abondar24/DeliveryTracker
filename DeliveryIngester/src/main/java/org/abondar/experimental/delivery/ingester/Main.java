package org.abondar.experimental.delivery.ingester;

import io.vertx.reactivex.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.INGESTER_PORT;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        var vertx = Vertx.vertx();

        vertx.rxDeployVerticle(new IngesterVerticle())
                .subscribe(
                        ok -> logger.info("Server up on port {}",INGESTER_PORT),
                        err -> logger.info("Error: ",err)
                );
    }
}
