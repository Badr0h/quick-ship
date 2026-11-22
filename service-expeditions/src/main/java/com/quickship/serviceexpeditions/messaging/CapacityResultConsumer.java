package com.quickship.serviceexpeditions.messaging;

import com.quickship.serviceexpeditions.entite.ProcessedEvent;
import com.quickship.serviceexpeditions.entite.Shipment;
import com.quickship.serviceexpeditions.messaging.event.ShipmentCapacityResultEvent;
import com.quickship.serviceexpeditions.repository.ProcessedEventRepository;
import com.quickship.serviceexpeditions.repository.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CapacityResultConsumer {
    private static final Logger log = LoggerFactory.getLogger(CapacityResultConsumer.class);
    private final ShipmentRepository shipmentRepository;
    private final ProcessedEventRepository processedEventRepository;

    public CapacityResultConsumer(ShipmentRepository shipmentRepository,
                                  ProcessedEventRepository processedEventRepository) {
        this.shipmentRepository = shipmentRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(topics = "${app.kafka.topic.shipment-capacity-reserved}",
            properties = "spring.json.value.default.type=com.quickship.serviceexpeditions.messaging.event.ShipmentCapacityResultEvent")
    @Transactional
    public void onReserved(@Payload ShipmentCapacityResultEvent event) {
        handle(event, true);
    }

    @KafkaListener(topics = "${app.kafka.topic.shipment-capacity-rejected}",
            properties = "spring.json.value.default.type=com.quickship.serviceexpeditions.messaging.event.ShipmentCapacityResultEvent")
    @Transactional
    public void onRejected(@Payload ShipmentCapacityResultEvent event) {
        handle(event, false);
    }

    private void handle(ShipmentCapacityResultEvent event, boolean reserved) {
        String eid = event.eventId().toString();
        if (processedEventRepository.existsByEventId(eid)) {
            log.info("[Kafka] Event déjà traité eventId={}", eid);
            return;
        }
        shipmentRepository.findById(event.shipmentId()).ifPresent(s -> {
            s.setCapaciteReservee(reserved);
            shipmentRepository.save(s);
            log.info("[Kafka] Shipment {} capaciteReservee={}", s.getId(), reserved);
        });
        processedEventRepository.save(new ProcessedEvent(eid, reserved ? "reserved" : "rejected"));
    }
}
