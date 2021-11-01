package org.abondar.experimental.delivery.stat;

import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(StatVerticle.class);

    @Override
    public Completable rxStart(){
        return Completable.complete();
    }
}
