package com.quickship.serviceexpeditions.messaging.event;

import java.util.UUID;

public record ShipmentCapacityResultEvent(
        UUID eventId,
        int eventVersion,
        String occurredAt,
        String correlationId,
        Long shipmentId,
        Long routeId,
        boolean reserved,
        String reason
) {}
