package com.quickship.serviceclients.repository;
import com.quickship.serviceclients.entite.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByEventId(String eventId);
}
