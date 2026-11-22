package com.quickship.serviceexpeditions.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaTopics {
    public final String shipmentRequested;
    public final String capacityReserved;
    public final String capacityRejected;
    public final String inTransit;
    public final String delivered;
    public final String returned;
    public final String lost;
    public final String notification;

    public KafkaTopics(
            @Value("${app.kafka.topic.shipment-requested}") String shipmentRequested,
            @Value("${app.kafka.topic.shipment-capacity-reserved}") String capacityReserved,
            @Value("${app.kafka.topic.shipment-capacity-rejected}") String capacityRejected,
            @Value("${app.kafka.topic.shipment-in-transit}") String inTransit,
            @Value("${app.kafka.topic.shipment-delivered}") String delivered,
            @Value("${app.kafka.topic.parcel-returned}") String returned,
            @Value("${app.kafka.topic.parcel-lost}") String lost,
            @Value("${app.kafka.topic.notification}") String notification) {
        this.shipmentRequested = shipmentRequested;
        this.capacityReserved = capacityReserved;
        this.capacityRejected = capacityRejected;
        this.inTransit = inTransit;
        this.delivered = delivered;
        this.returned = returned;
        this.lost = lost;
        this.notification = notification;
    }
}
