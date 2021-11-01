package org.abondar.experimental.delivery.notificator;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.MailConfig;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.ext.mail.MailClient;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.abondar.experimental.delivery.notificator.NotificatorUtil.EMAIL_PORT;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.KAFKA_CONFIG;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.KAFKA_TOPIC;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.SERVER_HOST;

public class NotificatorVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(NotificatorVerticle.class);

    @Override
    public Completable rxStart() {

        var mailClient = MailClient.createShared(vertx, mailConfig());
        var webClient = WebClient.create(vertx);
        var sender = new NotificationSender(mailClient, webClient);

        KafkaConsumer<String, JsonObject> consumer = KafkaConsumer.create(vertx, KAFKA_CONFIG);

        var conn = consumer.subscribe(KAFKA_TOPIC)
                .toFlowable()
                .replay();

        conn.filter(FilterUtil::deliveryTargetSuccess)
                .distinct(KafkaConsumerRecord::key)
                .flatMapSingle(record -> sender.sendEmail(record, true))
                .doOnError(err -> logger.error("Error: ", err))
                .retryWhen(this::retry)
                .subscribe(mailResult -> logger.info("Email sent: {}", mailResult.getRecipients()));


        conn.connect();

        conn.filter(FilterUtil::deliveryTargetFail)
                .distinct(KafkaConsumerRecord::key)
                .flatMapSingle(record -> sender.sendEmail(record, false))
                .doOnError(err -> logger.error("Error: ", err))
                .retryWhen(this::retry)
                .subscribe(mailResult -> logger.info("Email sent: {}", mailResult.getRecipients()));


        return Completable.complete();
    }

    private MailConfig mailConfig() {
        return new MailConfig()
                .setHostname(SERVER_HOST)
                .setPort(EMAIL_PORT);
    }


    private Flowable<Throwable> retry(Flowable<Throwable> err) {
        return err.delay(10, TimeUnit.SECONDS, RxHelper.scheduler(vertx));
    }
}
