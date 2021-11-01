package org.abondar.experimental.delivery.activity.verticle;

import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.abondar.experimental.delivery.activity.service.DatabaseService;
import org.abondar.experimental.delivery.activity.service.DatabaseServiceImpl;
import org.abondar.experimental.delivery.activity.util.DbUtil;

import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DAY_ENDPOINT;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.MONTH_ENDPOINT;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DISTANCE_RANKING_ENDPOINT;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.SERVER_PORT;
import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.TOTAL_ENDPOINT;

public class ActivityApiVerticle extends AbstractVerticle {

    private DatabaseService databaseService;

    public ActivityApiVerticle(){}

    public ActivityApiVerticle(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public Completable rxStart(){
        var router = Router.router(vertx);

        if (databaseService==null){
            var pgPool = PgPool.pool(vertx, DbUtil.PG_OPTS,new PoolOptions());
            databaseService = new DatabaseServiceImpl(pgPool);
        }
        var handler = new ApiHandler(databaseService);

        router.get(TOTAL_ENDPOINT).handler(handler::handleTotal);
        router.get(MONTH_ENDPOINT).handler(handler::handleMonth);
        router.get(DAY_ENDPOINT).handler(handler::handleDay);
        router.get(DISTANCE_RANKING_ENDPOINT).handler(handler::handleRanking);

        return vertx.createHttpServer()
                .requestHandler(router)
                .rxListen(SERVER_PORT)
                .ignoreElement();
    }
}
