package com.quickship.serviceexpeditions.messaging.event;

import java.util.UUID;

public record NotificationEmailRequestedEvent(
        UUID eventId,
        int eventVersion,
        String occurredAt,
        String correlationId,
        Long shipmentId,
        String email,
        String sujet,
        String contenu
) {}
