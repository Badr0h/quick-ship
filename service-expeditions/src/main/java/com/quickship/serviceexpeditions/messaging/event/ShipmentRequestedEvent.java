package com.quickship.serviceexpeditions.messaging.event;

import java.util.UUID;

public record ShipmentRequestedEvent(
        UUID eventId,
        int eventVersion,
        String occurredAt,
        String correlationId,
        Long shipmentId,
        Long routeId,
        Long customerId
) {}
