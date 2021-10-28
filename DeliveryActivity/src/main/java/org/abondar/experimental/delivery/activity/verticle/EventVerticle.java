package org.abondar.experimental.delivery.activity.verticle;

import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;

public class EventVerticle extends AbstractVerticle {

    @Override
    public Completable rxStart(){
        return Completable.complete();
    }
}
