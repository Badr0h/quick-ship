package com.quickship.serviceexpeditions.repository;
import com.quickship.serviceexpeditions.entite.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByEventId(String eventId);
}
