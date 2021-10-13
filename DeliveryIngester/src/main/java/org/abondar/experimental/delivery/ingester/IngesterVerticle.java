package org.abondar.experimental.delivery.ingester;

import io.reactivex.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.kafka.client.producer.KafkaProducer;
import org.abondar.experimental.delivery.ingester.util.IngesterUtil;
import org.abondar.experimental.delivery.ingester.util.VerificationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IngesterVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(IngesterVerticle.class);

    @Override
    public Completable rxStart() {

        var producer = IngesterKafkaProducer.initProducer(vertx);
        var amqpClient = new IngesterAmqpClient(vertx, producer);
        amqpClient.initClient();

        var router = Router.router(vertx);
        router.post()
                .handler(BodyHandler.create());

        router.post("/ingest")
                .handler((ctx) -> handleIngest(ctx, producer));
        return vertx.createHttpServer()
                .requestHandler(router)
                .rxListen(IngesterUtil.INGESTER_PORT)
                .ignoreElement();
    }


    private void handleIngest(RoutingContext ctx, KafkaProducer<String, JsonObject> producer) {
        var payload = ctx.getBodyAsJson();
        if (VerificationUtil.isInvalidPayload(payload)){
            ctx.fail(400);
        }

        var record = IngesterKafkaProducer.makeRecord(payload);
        producer.rxSend(record)
                .subscribe(
                        ok -> ctx.response().end(),
                        err -> {
                            logger.error("HTTP ingestion failed",err);
                            ctx.fail(500);
                        }
                );
    }
}
