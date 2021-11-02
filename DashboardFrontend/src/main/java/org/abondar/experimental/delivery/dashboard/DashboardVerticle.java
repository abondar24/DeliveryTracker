package org.abondar.experimental.delivery.dashboard;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.internal.functions.Functions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.abondar.experimental.delivery.dashboard.util.DashboardUtil.ACTIVITY_HOST;
import static org.abondar.experimental.delivery.dashboard.util.DashboardUtil.ACTIVITY_PORT;
import static org.abondar.experimental.delivery.dashboard.util.DashboardUtil.DEVICE_ENDPOINT;
import static org.abondar.experimental.delivery.dashboard.util.DashboardUtil.DISTANCE_RANKING_ENDPOINT;
import static org.abondar.experimental.delivery.dashboard.util.DashboardUtil.EVENTBUS_ADDR_REGEX;
import static org.abondar.experimental.delivery.dashboard.util.DashboardUtil.EVENTBUS_RANKING;
import static org.abondar.experimental.delivery.dashboard.util.DashboardUtil.EVENTBUS_ROUTE;
import static org.abondar.experimental.delivery.dashboard.util.DashboardUtil.EVENTBUS_THROUGHPUT;
import static org.abondar.experimental.delivery.dashboard.util.DashboardUtil.EVENTBUS_TREND;
import static org.abondar.experimental.delivery.dashboard.util.DashboardUtil.RETRY_TIMEOUT;
import static org.abondar.experimental.delivery.dashboard.util.DashboardUtil.SERVER_PORT;
import static org.abondar.experimental.delivery.dashboard.util.DashboardUtil.USER_SERVICE_HOST;
import static org.abondar.experimental.delivery.dashboard.util.DashboardUtil.USER_SERVICE_PORT;
import static org.abondar.experimental.delivery.dashboard.util.FieldUtil.DELIVERED_FIELD;
import static org.abondar.experimental.delivery.dashboard.util.FieldUtil.DEVICE_ID_FIELD;
import static org.abondar.experimental.delivery.dashboard.util.FieldUtil.DISTANCE_FIELD;
import static org.abondar.experimental.delivery.dashboard.util.FieldUtil.GARAGE_FIELD;
import static org.abondar.experimental.delivery.dashboard.util.FieldUtil.TIMESTAMP_FIELD;
import static org.abondar.experimental.delivery.dashboard.util.FieldUtil.USERNAME_FIELD;
import static org.abondar.experimental.delivery.dashboard.util.KafkaUtil.RANKING_GROUP;
import static org.abondar.experimental.delivery.dashboard.util.KafkaUtil.STAT_ACTIVITY_TOPIC;
import static org.abondar.experimental.delivery.dashboard.util.KafkaUtil.STAT_GARAGE_TREND_TOPIC;
import static org.abondar.experimental.delivery.dashboard.util.KafkaUtil.THROUGHPUT_GROUP;
import static org.abondar.experimental.delivery.dashboard.util.KafkaUtil.THROUGHPUT_TOPIC;
import static org.abondar.experimental.delivery.dashboard.util.KafkaUtil.TREND_GROUP;
import static org.abondar.experimental.delivery.dashboard.util.KafkaUtil.consumerConfig;

public class DashboardVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DashboardVerticle.class);

    private final Map<String, JsonObject> distanceRanking = new HashMap<>();

    @Override
    public Completable rxStart() {
        KafkaConsumer<String, JsonObject> thConsumer = KafkaConsumer.create(vertx, consumerConfig(THROUGHPUT_GROUP));
        thConsumer.subscribe(THROUGHPUT_TOPIC)
                .toFlowable()
                .subscribe(record -> forwardRecord(record, EVENTBUS_THROUGHPUT));

        KafkaConsumer<String, JsonObject> trConsumer = KafkaConsumer.create(vertx, consumerConfig(TREND_GROUP));
        trConsumer.subscribe(STAT_GARAGE_TREND_TOPIC)
                .toFlowable()
                .subscribe(record -> forwardRecord(record, EVENTBUS_TREND));


        KafkaConsumer<String, JsonObject> rankConsumer = KafkaConsumer.create(vertx, consumerConfig(RANKING_GROUP));
        rankConsumer.subscribe(STAT_ACTIVITY_TOPIC)
                .toFlowable()
                .buffer(5, TimeUnit.SECONDS, RxHelper.scheduler(vertx))
                .subscribe(this::updateRanking);

        hydrate();

        var sockJsHandler = SockJSHandler.create(vertx);
        var bridgeOpts = new SockJSBridgeOptions();
        bridgeOpts.addInboundPermitted(new PermittedOptions().setAddressRegex(EVENTBUS_ADDR_REGEX));
        bridgeOpts.addOutboundPermitted(new PermittedOptions().setAddressRegex(EVENTBUS_ADDR_REGEX));
        sockJsHandler.bridge(bridgeOpts);


        var router = Router.router(vertx);

        router.route()
                .handler(StaticHandler.create("webroot/assets"));
        router.get("/*")
                .handler(ctx -> ctx.reroute("/index.html"));
        router.route(EVENTBUS_ROUTE).handler(sockJsHandler);

        return vertx.createHttpServer()
                .requestHandler(router)
                .rxListen(SERVER_PORT)
                .ignoreElement();
    }

    private void forwardRecord(KafkaConsumerRecord<String, JsonObject> record, String destination) {
        vertx.eventBus().publish(destination, record.value());
    }

    private void updateRanking(List<KafkaConsumerRecord<String, JsonObject>> records) {
        copyScores(records);
        pruneEntries();
        vertx.eventBus().publish(EVENTBUS_RANKING, computeRanking());
    }

    private void copyScores(List<KafkaConsumerRecord<String, JsonObject>> records) {
        records.forEach(rec -> {
            var json = rec.value();
            var distance = json.getInteger(DISTANCE_FIELD);
            var username = json.getString(USERNAME_FIELD);
            var previousData = distanceRanking.get(username);

            if (previousData == null || previousData.getInteger(DELIVERED_FIELD) < distance) {
                distanceRanking.put(username, json);
            }
        });
    }

    private void pruneEntries(){
        var now = Instant.now();
        var iterator = distanceRanking.entrySet().iterator();

        while (iterator.hasNext()){
            var entry = iterator.next();
            var timestamp = Instant.parse(entry.getValue().getString(TIMESTAMP_FIELD));
            if (timestamp.until(now, ChronoUnit.DAYS)>=1L){
                iterator.remove();
            }
        }
    }

    private JsonArray computeRanking(){
        var ranking = distanceRanking.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .sorted(this::compareDistance)
                .map(jsonObject -> {
                    var sortedJson = new JsonObject();
                    sortedJson.put(USERNAME_FIELD,jsonObject.getString(USERNAME_FIELD));
                    sortedJson.put(DELIVERED_FIELD,jsonObject.getString(DELIVERED_FIELD));
                    sortedJson.put(DISTANCE_FIELD,jsonObject.getString(DISTANCE_FIELD));
                    sortedJson.put(GARAGE_FIELD,jsonObject.getString(GARAGE_FIELD));

                    return sortedJson;
                })
                .collect(Collectors.toList());

        return new JsonArray(ranking);
    }

    private int compareDistance(JsonObject a,JsonObject b){
        var first = a.getInteger(DISTANCE_FIELD);
        var second = b.getInteger(DISTANCE_FIELD);
        return second.compareTo(first);
    }

    private void hydrate(){
        var webClient = WebClient.create(vertx);
        webClient.get(ACTIVITY_PORT,ACTIVITY_HOST,DISTANCE_RANKING_ENDPOINT)
                .as(BodyCodec.jsonArray())
                .rxSend()
                .delay(RETRY_TIMEOUT,TimeUnit.SECONDS,RxHelper.scheduler(vertx))
                .retry(RETRY_TIMEOUT)
                .map(HttpResponse::body)
                .flattenAsFlowable(Functions.identity())
                .cast(JsonObject.class)
                .flatMapSingle(json->fetchDevice(webClient,json))
                .flatMapSingle(json->fillUserData(webClient,json))
                .subscribe(this::updateRanking,
                        err-> logger.error("Hydration Error: ",err),
                        ()->logger.info("Hydration Completed"));
    }

    private Single<JsonObject> fetchDevice(WebClient client,JsonObject json){
        var deviceId = json.getString(DEVICE_ID_FIELD);
        return client.get(USER_SERVICE_PORT,USER_SERVICE_HOST,DEVICE_ENDPOINT+"/"+deviceId)
                .as(BodyCodec.jsonObject())
                .rxSend()
                .retry(RETRY_TIMEOUT)
                .map(HttpResponse::body)
                .map(resp->resp.mergeIn(json));
    }

    private Single<JsonObject> fillUserData(WebClient client,JsonObject json){
        var username = json.getString(USERNAME_FIELD);
        return client.get(USER_SERVICE_PORT,USER_SERVICE_HOST,"/"+username)
                .as(BodyCodec.jsonObject())
                .rxSend()
                .retry(RETRY_TIMEOUT)
                .map(HttpResponse::body)
                .map(resp->resp.mergeIn(json));
    }

    private void updateRanking(JsonObject data){
        data.put(TIMESTAMP_FIELD,Instant.now().toString());
        distanceRanking.put(data.getString(USERNAME_FIELD),data);
    }


}
