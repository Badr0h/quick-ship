package com.quickship.servicereseau.messaging;

import com.quickship.servicereseau.messaging.event.ShipmentCapacityResultEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ReseauEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(ReseauEventPublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topicReserved;
    private final String topicRejected;

    public ReseauEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                @Value("${app.kafka.topic.shipment-capacity-reserved}") String topicReserved,
                                @Value("${app.kafka.topic.shipment-capacity-rejected}") String topicRejected) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicReserved = topicReserved;
        this.topicRejected = topicRejected;
    }

    public void publishResult(Long shipmentId, Long routeId, String correlationId, boolean reserved, String reason) {
        ShipmentCapacityResultEvent event = new ShipmentCapacityResultEvent(
                UUID.randomUUID(), 1, Instant.now().toString(), correlationId, shipmentId, routeId, reserved, reason);
        String topic = reserved ? topicReserved : topicRejected;
        kafkaTemplate.send(topic, shipmentId.toString(), event).whenComplete((r, ex) -> {
            if (ex != null) log.error("[Kafka] Échec {} : {}", topic, ex.getMessage());
            else log.info("[Kafka] Publié {} shipmentId={}", topic, shipmentId);
        });
    }
}
