package org.abondar.experimental.delivery.notificator;

import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificatorVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(NotificatorVerticle.class);

    @Override
    public Completable rxStart() {
        return null;
    }
}
