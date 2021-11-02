package org.abondar.experimental.delivery.dashboard;

import io.vertx.reactivex.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.abondar.experimental.delivery.dashboard.util.DashboardUtil.SERVER_PORT;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        var vertx = Vertx.vertx();
        vertx
                .rxDeployVerticle(new DashboardVerticle())
                .subscribe(
                        ok -> logger.info("HTTP server is running on port {}", SERVER_PORT),
                        err -> logger.error("Error: ", err));
    }
}
