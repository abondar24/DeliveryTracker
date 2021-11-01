package org.abondar.experimental.delivery.activity.verticle;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;
import io.vertx.reactivex.kafka.client.producer.KafkaProducer;
import io.vertx.reactivex.kafka.client.producer.KafkaProducerRecord;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.abondar.experimental.delivery.activity.service.DatabaseService;
import org.abondar.experimental.delivery.activity.service.DatabaseServiceImpl;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.abondar.experimental.delivery.activity.util.ActivityApiUtil.DEVICE_ID_FIELD;
import static org.abondar.experimental.delivery.activity.util.DbUtil.PG_OPTS;
import static org.abondar.experimental.delivery.activity.util.KafkaUtil.CONSUMER_CONFIG;
import static org.abondar.experimental.delivery.activity.util.KafkaUtil.KAFKA_CONSUMER_TOPIC;
import static org.abondar.experimental.delivery.activity.util.KafkaUtil.KAFKA_PRODUCER_TOPIC;
import static org.abondar.experimental.delivery.activity.util.KafkaUtil.PRODUCER_CONFIG;

public class EventVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(EventVerticle.class);
    private DatabaseService databaseService;

    private KafkaConsumer<String, JsonObject> consumer;


    public EventVerticle() {
    }

    public EventVerticle(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public Completable rxStart() {
        if (databaseService == null) {
            var pool = PgPool.pool(vertx, PG_OPTS, new PoolOptions());
            databaseService = new DatabaseServiceImpl(pool);
        }

        consumer = KafkaConsumer.create(vertx, CONSUMER_CONFIG);

        consumer.subscribe(KAFKA_CONSUMER_TOPIC)
                .toFlowable()
                .flatMap(this::insertRecord)
                .flatMap(this::getActivityUpdate)
                .flatMap(this::commitKafkaConsumerOffset)
                .retryWhen(this::retry)
                .subscribe();


        return Completable.complete();
    }

    private Flowable<KafkaConsumerRecord<String, JsonObject>> insertRecord(KafkaConsumerRecord<String, JsonObject> record) {
        var data = record.value();

        return databaseService.insertDelivery(data)
                .map(rowSet -> record)
                .toFlowable();
    }

    private Flowable<KafkaConsumerRecord<String, JsonObject>> getActivityUpdate(KafkaConsumerRecord<String, JsonObject> record) {
        KafkaProducer<String, JsonObject> producer = KafkaProducer.create(vertx, PRODUCER_CONFIG);

        var deviceId = record.value().getString(DEVICE_ID_FIELD);
        var key = generateKey(deviceId);

        return databaseService.getTodayUpdate(deviceId)
                .flatMap(json-> producer.rxSend(KafkaProducerRecord.create(KAFKA_PRODUCER_TOPIC,key,json)))
                .map(rs -> record)
                .toFlowable();
    }

    private String generateKey(String deviceId) {
        var now = LocalDateTime.now();

        return deviceId + ":" + now.getYear() + "-"
                + now.getDayOfMonth() + 1 + "-" + now.getDayOfMonth();
    }

    private Flowable<Throwable> retry(Flowable<Throwable> err) {
        return err.delay(10, TimeUnit.SECONDS, RxHelper.scheduler(vertx));
    }

    private Publisher<?> commitKafkaConsumerOffset(KafkaConsumerRecord<String, JsonObject> record) {
        return consumer.rxCommit().toFlowable();
    }
}
