package com.quickship.serviceexpeditions.messaging.event;

import java.math.BigDecimal;
import java.util.UUID;

public record ShipmentLifecycleEvent(
        UUID eventId,
        int eventVersion,
        String occurredAt,
        String correlationId,
        Long shipmentId,
        Long routeId,
        Long customerId,
        BigDecimal montantTransport,
        String email,
        String customerNom,
        String numeroSuivi,
        String statut
) {}
