package com.quickship.servicenotification.messaging;

import com.quickship.servicenotification.dto.NotificationEmailDto;
import com.quickship.servicenotification.entite.ProcessedEvent;
import com.quickship.servicenotification.messaging.event.NotificationEmailRequestedEvent;
import com.quickship.servicenotification.repository.ProcessedEventRepository;
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
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private final NotificationService notificationService;
    private final Validator validator;
    private final ProcessedEventRepository processedEventRepository;

    public NotificationConsumer(NotificationService notificationService, Validator validator,
                                ProcessedEventRepository processedEventRepository) {
        this.notificationService = notificationService;
        this.validator = validator;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(topics = "${app.kafka.topic.notification}",
            properties = "spring.json.value.default.type=com.quickship.servicenotification.messaging.event.NotificationEmailRequestedEvent")
    @Transactional
    public void recevoir(
            @Payload NotificationEmailRequestedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        String eid = event.eventId().toString();
        if (processedEventRepository.existsByEventId(eid)) {
            log.info("[Kafka] Event déjà traité eventId={} — e-mail non renvoyé", eid);
            return;
        }
        log.info("[Kafka] Message reçu eventId={} shipmentId={} partition={} offset={}",
                event.eventId(), event.shipmentId(), partition, offset);
        NotificationEmailDto dto = new NotificationEmailDto(event.email(), event.sujet(), event.contenu());
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("Événement invalide eventId=" + event.eventId(), violations);
        }
        notificationService.envoyerEmail(dto);
        processedEventRepository.save(new ProcessedEvent(eid, "notification.email.requested"));
        log.info("[Kafka] E-mail envoyé eventId={} destinataire={}", event.eventId(), event.email());
    }
}
