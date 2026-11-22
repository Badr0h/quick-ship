package com.quickship.serviceexpeditions.messaging;

import com.quickship.serviceexpeditions.messaging.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class ShipmentEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(ShipmentEventPublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopics topics;

    public ShipmentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate, KafkaTopics topics) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
    }

    public void publishRequested(Long shipmentId, Long routeId, Long customerId, String correlationId) {
        ShipmentRequestedEvent event = new ShipmentRequestedEvent(
                UUID.randomUUID(), 1, Instant.now().toString(), correlationId, shipmentId, routeId, customerId);
        send(topics.shipmentRequested, shipmentId, event);
    }

    public void publishInTransit(Long shipmentId, Long routeId, String correlationId) {
        ShipmentLifecycleEvent event = new ShipmentLifecycleEvent(
                UUID.randomUUID(), 1, Instant.now().toString(), correlationId,
                shipmentId, routeId, null, null, null, null, null, "EN_TRANSIT");
        send(topics.inTransit, shipmentId, event);
    }

    public void publishDelivered(Long shipmentId, Long routeId, Long customerId, BigDecimal montant,
                                 String email, String nom, String numero, String correlationId) {
        ShipmentLifecycleEvent event = new ShipmentLifecycleEvent(
                UUID.randomUUID(), 1, Instant.now().toString(), correlationId,
                shipmentId, routeId, customerId, montant, email, nom, numero, "LIVRE");
        send(topics.delivered, shipmentId, event);
        publishNotification(shipmentId, correlationId, email,
                "Colis livré " + numero,
                "Bonjour " + nom + ", votre colis " + numero + " a été livré.");
    }

    public void publishReturnedOrLost(Long shipmentId, Long routeId, Long customerId, BigDecimal montant,
                                      String email, String nom, String numero, String statut, String correlationId) {
        String topic = "RETOUR".equals(statut) ? topics.returned : topics.lost;
        ShipmentLifecycleEvent event = new ShipmentLifecycleEvent(
                UUID.randomUUID(), 1, Instant.now().toString(), correlationId,
                shipmentId, routeId, customerId, montant, email, nom, numero, statut);
        send(topic, shipmentId, event);
        publishNotification(shipmentId, correlationId, email,
                "Colis " + statut + " " + numero,
                "Bonjour " + nom + ", votre colis " + numero + " est passé en statut " + statut + ".");
    }

    public void publishNotification(Long shipmentId, String correlationId, String email, String sujet, String contenu) {
        NotificationEmailRequestedEvent event = new NotificationEmailRequestedEvent(
                UUID.randomUUID(), 1, Instant.now().toString(), correlationId, shipmentId, email, sujet, contenu);
        send(topics.notification, shipmentId, event);
    }

    private void send(String topic, Long shipmentId, Object event) {
        String key = shipmentId.toString();
        kafkaTemplate.send(topic, key, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("[Kafka] Échec publication topic={} shipmentId={} : {}", topic, shipmentId, ex.getMessage());
            } else {
                log.info("[Kafka] Publié topic={} shipmentId={} partition={} offset={}",
                        topic, shipmentId, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            }
        });
    }
}
