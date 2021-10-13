package org.abondar.experimental.delivery.ingester;


import io.reactivex.Flowable;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.amqp.AmqpReceiverOptions;
import io.vertx.core.json.JsonObject;

import io.vertx.reactivex.amqp.AmqpClient;
import io.vertx.reactivex.amqp.AmqpMessage;
import io.vertx.reactivex.amqp.AmqpReceiver;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.kafka.client.producer.KafkaProducer;
import org.abondar.experimental.delivery.ingester.util.VerificationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.AMQP_EVENT;
import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.AMQP_PORT;
import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.AMQ_USER;
import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.CONTENT_TYPE;
import static org.abondar.experimental.delivery.ingester.util.IngesterUtil.SERVER_HOST;

public class IngesterAmqpClient {

    private static final Logger logger = LoggerFactory.getLogger(IngesterAmqpClient.class);

    private final Vertx vertx;

    private final KafkaProducer<String, JsonObject> kafkaProducer;

    public IngesterAmqpClient(Vertx vertx, KafkaProducer<String, JsonObject> kafkaProducer) {
        this.vertx = vertx;
        this.kafkaProducer = kafkaProducer;
    }

    public void initClient() {
        var clientOpts = configClient();
        var receiverOpts = configReceiver();

        AmqpClient.create(vertx, clientOpts)
                .rxConnect()
                .flatMap(conn -> conn.rxCreateReceiver(AMQP_EVENT, receiverOpts))
                .flatMapPublisher(AmqpReceiver::toFlowable)
                .doOnError(this::logError)
                .retryWhen(this::retryLater)
                .subscribe(this::handleMessage);
    }


    private AmqpClientOptions configClient() {
        return new AmqpClientOptions()
                .setHost(SERVER_HOST)
                .setPort(AMQP_PORT)
                .setUsername(AMQ_USER);
    }

    private AmqpReceiverOptions configReceiver() {
        return new AmqpReceiverOptions()
                .setAutoAcknowledgement(false)
                .setDurable(true);
    }

    private void logError(Throwable err) {
        logger.error("Error", err);
    }

    private Flowable<Throwable> retryLater(Flowable<Throwable> errors) {
        return errors.debounce(10, TimeUnit.SECONDS, RxHelper.scheduler(vertx));
    }

    private void handleMessage(AmqpMessage message) {
        var payload = message.bodyAsJsonObject();
        if (!message.contentType()
                .equals(CONTENT_TYPE) || VerificationUtil.isInvalidPayload(payload)) {
            logger.error("Ivalid AMQP message: {}", message.bodyAsBinary());
            message.accepted();
            return;
        }

        var record = IngesterKafkaProducer.makeRecord(payload);
        kafkaProducer.rxSend(record)
                .subscribe(
                        ok -> message.accepted(),
                        err ->{
                            logger.error("AMQP ingestion failed",err);
                            message.rejected();
                        }
                );
    }
}
