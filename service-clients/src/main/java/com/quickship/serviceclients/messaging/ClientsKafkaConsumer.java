package com.quickship.serviceclients.messaging;

import com.quickship.serviceclients.entite.Claim;
import com.quickship.serviceclients.entite.ProcessedEvent;
import com.quickship.serviceclients.messaging.event.ShipmentLifecycleEvent;
import com.quickship.serviceclients.repository.ClaimRepository;
import com.quickship.serviceclients.repository.ProcessedEventRepository;
import com.quickship.serviceclients.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientsKafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(ClientsKafkaConsumer.class);
    private final CustomerService customerService;
    private final ClaimRepository claimRepository;
    private final ProcessedEventRepository processedEventRepository;

    public ClientsKafkaConsumer(CustomerService customerService, ClaimRepository claimRepository,
                                ProcessedEventRepository processedEventRepository) {
        this.customerService = customerService;
        this.claimRepository = claimRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(topics = "${app.kafka.topic.shipment-delivered}",
            properties = "spring.json.value.default.type=com.quickship.serviceclients.messaging.event.ShipmentLifecycleEvent")
    @Transactional
    public void onDelivered(@Payload ShipmentLifecycleEvent event) {
        String eid = event.eventId().toString();
        if (processedEventRepository.existsByEventId(eid)) return;
        customerService.augmenterEncours(event.customerId(), event.montantTransport());
        processedEventRepository.save(new ProcessedEvent(eid, "shipment.delivered"));
        log.info("[Kafka] Encours augmenté customerId={} montant={}", event.customerId(), event.montantTransport());
    }

    @KafkaListener(topics = {
            "${app.kafka.topic.parcel-returned}",
            "${app.kafka.topic.parcel-lost}"
    }, properties = "spring.json.value.default.type=com.quickship.serviceclients.messaging.event.ShipmentLifecycleEvent")
    @Transactional
    public void onReturnedOrLost(@Payload ShipmentLifecycleEvent event) {
        String eid = event.eventId().toString();
        if (processedEventRepository.existsByEventId(eid)) return;
        if (!claimRepository.existsByShipmentId(event.shipmentId())) {
            Claim claim = new Claim();
            claim.setCustomerId(event.customerId());
            claim.setShipmentId(event.shipmentId());
            claim.setDescription("Réclamation automatique : colis " + event.statut());
            claim.setMontant(event.montantTransport());
            claim.setStatut("OUVERTE");
            claimRepository.save(claim);
            log.info("[Kafka] Claim ouverte shipmentId={}", event.shipmentId());
        } else {
            log.info("[Kafka] Claim déjà existante shipmentId={} — dédupliquée", event.shipmentId());
        }
        processedEventRepository.save(new ProcessedEvent(eid, "parcel.claim"));
    }
}
