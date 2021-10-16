package org.abondar.experimental.delivery.notificator;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.MailConfig;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mail.MailClient;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.abondar.experimental.delivery.notificator.NotificatorUtil.DELIVERY_MINUMUM;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.DELIVERY_TARGET;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.DISTANCE_MINIMUM;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.DISTANCE_TARGET;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.KAFKA_CONFIG;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.KAFKA_TOPIC;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.MAIL_PORT;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.SERVER_HOST;

public class NotificatorVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(NotificatorVerticle.class);

    @Override
    public Completable rxStart() {

        var mailClient = MailClient.createShared(Vertx.vertx(), mailConfig());
        var webClient = WebClient.create(vertx);
        var sender = new NotificationSender(mailClient, webClient);

        KafkaConsumer<String, JsonObject> consumer = KafkaConsumer.create(vertx, KAFKA_CONFIG);

        var conn = consumer.subscribe(KAFKA_TOPIC)
                .toFlowable()
                .replay();

        conn.filter(this::deliveryTargetSuccess)
                .distinct(KafkaConsumerRecord::key)
                .flatMapSingle(record -> sender.sendEmail(record, true))
                .doOnError(err -> logger.error("Error: ", err))
                .retryWhen(this::retry)
                .subscribe(mailResult -> logger.info("Email sent:", mailResult.getRecipients()));


        conn.connect();

        conn.filter(this::deliveryTargetFail)
                .distinct(KafkaConsumerRecord::key)
                .flatMapSingle(record -> sender.sendEmail(record, false))
                .doOnError(err -> logger.error("Error: ", err))
                .retryWhen(this::retry)
                .subscribe(mailResult -> logger.info("Email sent:", mailResult.getRecipients()));


        return Completable.complete();
    }

    private MailConfig mailConfig() {
        return new MailConfig()
                .setHostname(SERVER_HOST)
                .setPort(MAIL_PORT);
    }

    private boolean deliveryTargetSuccess(KafkaConsumerRecord<String, JsonObject> record) {
        var val = record.value();

        return val.getInteger("delivered") >= DELIVERY_TARGET ||
                val.getInteger("distance") >= DISTANCE_TARGET;
    }

    private boolean deliveryTargetFail(KafkaConsumerRecord<String, JsonObject> record) {
        var val = record.value();

        return val.getInteger("delivered") < DELIVERY_MINUMUM ||
                val.getInteger("distance") >= DISTANCE_MINIMUM;
    }

    private Flowable<Throwable> retry(Flowable<Throwable> err) {
        return err.delay(10, TimeUnit.SECONDS, RxHelper.scheduler(vertx));
    }
}
