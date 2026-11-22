package com.quickship.servicereseau.repository;
import com.quickship.servicereseau.entite.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByEventId(String eventId);
}
