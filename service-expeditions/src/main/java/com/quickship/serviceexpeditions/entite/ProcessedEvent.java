package com.quickship.serviceexpeditions.entite;

import jakarta.persistence.*;

@Entity
@Table(name = "processed_event", uniqueConstraints = @UniqueConstraint(columnNames = "eventId"))
public class ProcessedEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 64)
    private String eventId;
    private String topic;

    public ProcessedEvent() {}
    public ProcessedEvent(String eventId, String topic) { this.eventId = eventId; this.topic = topic; }
    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public String getTopic() { return topic; }
}
