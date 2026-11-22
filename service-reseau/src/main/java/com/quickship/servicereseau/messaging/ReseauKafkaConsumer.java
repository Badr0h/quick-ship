package com.quickship.servicereseau.messaging;

import com.quickship.servicereseau.entite.ProcessedEvent;
import com.quickship.servicereseau.entite.Route;
import com.quickship.servicereseau.messaging.event.ShipmentLifecycleEvent;
import com.quickship.servicereseau.messaging.event.ShipmentRequestedEvent;
import com.quickship.servicereseau.repository.ProcessedEventRepository;
import com.quickship.servicereseau.service.RouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReseauKafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(ReseauKafkaConsumer.class);
    private final RouteService routeService;
    private final ReseauEventPublisher publisher;
    private final ProcessedEventRepository processedEventRepository;

    public ReseauKafkaConsumer(RouteService routeService, ReseauEventPublisher publisher,
                               ProcessedEventRepository processedEventRepository) {
        this.routeService = routeService;
        this.publisher = publisher;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(topics = "${app.kafka.topic.shipment-requested}",
            properties = "spring.json.value.default.type=com.quickship.servicereseau.messaging.event.ShipmentRequestedEvent")
    @Transactional
    public void onRequested(@Payload ShipmentRequestedEvent event) {
        String eid = event.eventId().toString();
        if (processedEventRepository.existsByEventId(eid)) return;
        try {
            routeService.reserverCapacitePourShipment(event.routeId(), event.shipmentId());
            publisher.publishResult(event.shipmentId(), event.routeId(), event.correlationId(), true, "OK");
        } catch (RuntimeException ex) {
            publisher.publishResult(event.shipmentId(), event.routeId(), event.correlationId(), false, ex.getMessage());
        }
        processedEventRepository.save(new ProcessedEvent(eid, "shipment.requested"));
    }

    @KafkaListener(topics = "${app.kafka.topic.shipment-in-transit}",
            properties = "spring.json.value.default.type=com.quickship.servicereseau.messaging.event.ShipmentLifecycleEvent")
    @Transactional
    public void onInTransit(@Payload ShipmentLifecycleEvent event) {
        String eid = event.eventId().toString();
        if (processedEventRepository.existsByEventId(eid)) return;
        // Validation sans second décompte
        Route route = routeService.getById(event.routeId());
        log.info("[Kafka] Affectation validée route={} capaciteReservee={} (pas de second décompte)",
                route.getId(), route.getCapaciteReservee());
        processedEventRepository.save(new ProcessedEvent(eid, "shipment.in-transit"));
    }

    @KafkaListener(topics = {
            "${app.kafka.topic.shipment-delivered}",
            "${app.kafka.topic.parcel-returned}",
            "${app.kafka.topic.parcel-lost}"
    }, properties = "spring.json.value.default.type=com.quickship.servicereseau.messaging.event.ShipmentLifecycleEvent")
    @Transactional
    public void onRelease(@Payload ShipmentLifecycleEvent event) {
        String eid = event.eventId().toString();
        if (processedEventRepository.existsByEventId(eid)) return;
        boolean liberated = routeService.libererCapacitePourShipment(event.shipmentId());
        processedEventRepository.save(new ProcessedEvent(eid, "capacity.release"));
        if (liberated) {
            log.info("[Kafka] Capacité libérée route={} shipmentId={}", event.routeId(), event.shipmentId());
        } else {
            log.info("[Kafka] Pas de libération (non réservé ou déjà libéré) shipmentId={}", event.shipmentId());
        }
    }
}
