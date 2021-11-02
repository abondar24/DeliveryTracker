package org.abondar.experimental.delivery.stat;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;
import io.vertx.reactivex.kafka.client.producer.KafkaProducer;
import io.vertx.reactivex.kafka.client.producer.KafkaProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.abondar.experimental.delivery.stat.util.FieldUtil.COUNT_FIELD;
import static org.abondar.experimental.delivery.stat.util.FieldUtil.DELIVERED_FIELD;
import static org.abondar.experimental.delivery.stat.util.FieldUtil.DISTANCE_FIELD;
import static org.abondar.experimental.delivery.stat.util.FieldUtil.GARAGE_FIELD;
import static org.abondar.experimental.delivery.stat.util.FieldUtil.SECONDS_FIELD;
import static org.abondar.experimental.delivery.stat.util.FieldUtil.THROUGHPUT_FIELD;
import static org.abondar.experimental.delivery.stat.util.FieldUtil.TIMESTAMP_FIELD;
import static org.abondar.experimental.delivery.stat.util.FieldUtil.UPDATES_FIELD;
import static org.abondar.experimental.delivery.stat.util.FieldUtil.USERNAME_FIELD;
import static org.abondar.experimental.delivery.stat.util.KafkaUtil.DATA_TOPIC;
import static org.abondar.experimental.delivery.stat.util.KafkaUtil.PRODUCER_CONFIG;
import static org.abondar.experimental.delivery.stat.util.KafkaUtil.STAT_ACTIVITY_TOPIC;
import static org.abondar.experimental.delivery.stat.util.KafkaUtil.STAT_GARAGE_TREND_TOPIC;
import static org.abondar.experimental.delivery.stat.util.KafkaUtil.THROUGHPUT_TOPIC;
import static org.abondar.experimental.delivery.stat.util.KafkaUtil.UPDATE_TOPIC;
import static org.abondar.experimental.delivery.stat.util.KafkaUtil.consumerConfig;
import static org.abondar.experimental.delivery.stat.util.UserServiceUtil.USER_DEVICE_ENDPOINT;
import static org.abondar.experimental.delivery.stat.util.UserServiceUtil.USER_SERVICE_HOST;
import static org.abondar.experimental.delivery.stat.util.UserServiceUtil.USER_SERVICE_PORT;

public class StatVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(StatVerticle.class);

    private WebClient webClient;

    private KafkaProducer<String, JsonObject> producer;

    @Override
    public Completable rxStart() {
        webClient = WebClient.create(vertx);
        producer = KafkaProducer.create(vertx, PRODUCER_CONFIG);

        KafkaConsumer<String, JsonObject> throughputConsumer = KafkaConsumer
                .create(vertx, consumerConfig("stat-throughput"));

        throughputConsumer.subscribe(DATA_TOPIC)
                .toFlowable()
                .buffer(5, TimeUnit.SECONDS, RxHelper.scheduler(vertx))
                .flatMapCompletable(this::publishThroughput)
                .doOnError(err -> logger.error("Error: ", err))
                .retryWhen(this::retry)
                .subscribe();

        KafkaConsumer<String, JsonObject> updateConsumer = KafkaConsumer.create(vertx, consumerConfig("stat-update"));
        updateConsumer.subscribe(UPDATE_TOPIC)
                .toFlowable()
                .flatMapSingle(this::addDeviceOwner)
                .flatMapSingle(this::addOwnerData)
                .flatMapCompletable(this::publishActivityUpdate)
                .doOnError(err -> logger.error("Error: ", err))
                .retryWhen(this::retry)
                .subscribe();

        KafkaConsumer<String, JsonObject> trendConsumer = KafkaConsumer.create(vertx, consumerConfig("stat-garage-trends"));
        trendConsumer.subscribe(STAT_ACTIVITY_TOPIC)
                .toFlowable()
                .groupBy(this::getGarage)
                .flatMap(groupedFlowable -> groupedFlowable.buffer(5, TimeUnit.SECONDS, RxHelper.scheduler(vertx)))
                .flatMapCompletable(this::publishTrendUpdate)
                .doOnError(err -> logger.error("Error: ", err))
                .retryWhen(this::retry)
                .subscribe();

        return Completable.complete();
    }


    private Single<JsonObject> addDeviceOwner(KafkaConsumerRecord<String, JsonObject> record) {
        var data = record.value();

        return webClient
                .get(USER_SERVICE_PORT, USER_SERVICE_HOST, USER_DEVICE_ENDPOINT + data.getString(DISTANCE_FIELD))
                .as(BodyCodec.jsonObject())
                .rxSend()
                .map(HttpResponse::body)
                .map(data::mergeIn);

    }

    private Single<JsonObject> addOwnerData(JsonObject data) {
        var username = data.getString(USERNAME_FIELD);

        return webClient
                .get(USER_SERVICE_PORT, USER_SERVICE_HOST, "/" + username)
                .as(BodyCodec.jsonObject())
                .rxSend()
                .map(HttpResponse::body)
                .map(data::mergeIn);

    }

    private String getGarage(KafkaConsumerRecord<String, JsonObject> record) {
        return record.value().getString(GARAGE_FIELD);
    }

    private CompletableSource publishThroughput(List<KafkaConsumerRecord<String, JsonObject>> records) {
        var payload = new JsonObject();
        payload.put(SECONDS_FIELD, 5);
        payload.put(COUNT_FIELD, records.size());
        payload.put(THROUGHPUT_FIELD, (((double) records.size() / 5.0d)));

        KafkaProducerRecord<String, JsonObject> record = KafkaProducerRecord.create(THROUGHPUT_TOPIC, payload);
        return producer.rxWrite(record);
    }

    private CompletableSource publishActivityUpdate(JsonObject data) {
        return producer.rxWrite(
                KafkaProducerRecord.create(STAT_ACTIVITY_TOPIC, data.getString(USERNAME_FIELD), data));
    }

    private CompletableSource publishTrendUpdate(List<KafkaConsumerRecord<String, JsonObject>> records) {
        if (records.size() > 0) {
            var garage = getGarage(records.get(0));

            var delivered = records.stream()
                    .map(record -> record.value().getInteger(DELIVERED_FIELD))
                    .reduce(0, Integer::sum);

            var distance = records.stream()
                    .map(record -> record.value().getInteger(DISTANCE_FIELD))
                    .reduce(0, Integer::sum);

            var payload = new JsonObject();
            payload.put(TIMESTAMP_FIELD, LocalDateTime.now().toString());
            payload.put(SECONDS_FIELD, 5);
            payload.put(GARAGE_FIELD, garage);
            payload.put(DELIVERED_FIELD, delivered);
            payload.put(DISTANCE_FIELD, distance);
            payload.put(UPDATES_FIELD, records.size());

            KafkaProducerRecord<String, JsonObject> record = KafkaProducerRecord
                    .create(STAT_GARAGE_TREND_TOPIC, garage, payload);

            return producer.rxWrite(record);
        } else {
            return Completable.complete();
        }
    }


    private Flowable<Throwable> retry(Flowable<Throwable> errors) {
        return errors.delay(10, TimeUnit.SECONDS, RxHelper.scheduler(vertx));
    }
}
