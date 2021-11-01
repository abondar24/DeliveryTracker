package org.abondar.experimental.delivery.notificator;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.MailResult;
import io.vertx.reactivex.ext.mail.MailClient;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.client.predicate.ResponsePredicate;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.abondar.experimental.delivery.notificator.NotificatorUtil.DELIVERED_FIELD;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.DEVICE_ENDPOINT;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.DEVICE_ID_FIELD;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.DISTANCE_FIELD;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.EMAIL_FIELD;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.EMAIL_SUBJECT;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.SERVER_HOST;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.SYSTEM_ADDRESS;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.USERNAME_FIELD;
import static org.abondar.experimental.delivery.notificator.NotificatorUtil.USER_SERVICE_PORT;

public class NotificationSender {

    private static final Logger logger = LoggerFactory.getLogger(NotificationSender.class);

    private final MailClient mailClient;

    private final WebClient webClient;

    public NotificationSender(MailClient mailClient, WebClient webClient) {
        this.mailClient = mailClient;
        this.webClient = webClient;
    }

    public Single<MailResult> sendEmail(KafkaConsumerRecord<String, JsonObject> record, boolean positive) {
        var val = record.value();

        var deviceId = val.getString(DEVICE_ID_FIELD);
        var delivered = val.getInteger(DELIVERED_FIELD);
        var distance = val.getInteger(DISTANCE_FIELD);

        return webClient.get(USER_SERVICE_PORT, SERVER_HOST, DEVICE_ENDPOINT +"/"+ deviceId)
                .expect(ResponsePredicate.SC_SUCCESS)
                .as(BodyCodec.jsonObject())
                .rxSend()
                .doOnError(err-> logger.error("User Service Error: "+err.getMessage()))
                .map(HttpResponse::body)
                .map(json -> json.getString(USERNAME_FIELD))
                .flatMap(this::getEmailAddress)
                .map(email -> buildEmail(delivered, distance, email,positive))
                .flatMap(mailClient::rxSendMail)
                .doOnError(err-> logger.error("Email Server Error: "+err.getMessage()));
    }


    private Single<String> getEmailAddress(String username){
        return webClient.get(USER_SERVICE_PORT,SERVER_HOST,"/"+username)
                .as(BodyCodec.jsonObject())
                .rxSend()
                .map(HttpResponse::body)
                .map(json -> json.getString(EMAIL_FIELD));
    }

    private MailMessage buildEmail(Integer delivered, Integer distance, String email, boolean positive) {
        var text = "";

        if (positive){
            text = String.format("You have delivered %d with distance %d. Great work!\n",delivered,distance);
        } else {
            text = String.format("You have delivered %d with distance %d. Work harder!\n",delivered,distance);
        }

        return new MailMessage()
                .setFrom(SYSTEM_ADDRESS)
                .setTo(email)
                .setSubject(EMAIL_SUBJECT)
                .setText(text);
    }



}
