package com.quickship.servicenotification.messaging;

import com.quickship.servicenotification.dto.NotificationEmailDto;
import com.quickship.servicenotification.messaging.event.NotificationEmailRequestedEvent;
import com.quickship.servicenotification.service.NotificationService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private final NotificationService notificationService;
    private final Validator validator;

    public NotificationConsumer(NotificationService notificationService, Validator validator) {
        this.notificationService = notificationService;
        this.validator = validator;
    }

    @KafkaListener(topics = "${app.kafka.topic.notification}",
            properties = "spring.json.value.default.type=com.quickship.servicenotification.messaging.event.NotificationEmailRequestedEvent")
    public void recevoir(
            @Payload NotificationEmailRequestedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("[Kafka] Message reçu eventId={} shipmentId={} partition={} offset={}",
                event.eventId(), event.shipmentId(), partition, offset);
        NotificationEmailDto dto = new NotificationEmailDto(event.email(), event.sujet(), event.contenu());
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("Événement invalide eventId=" + event.eventId(), violations);
        }
        notificationService.envoyerEmail(dto);
        log.info("[Kafka] E-mail envoyé eventId={} destinataire={}", event.eventId(), event.email());
    }
}
