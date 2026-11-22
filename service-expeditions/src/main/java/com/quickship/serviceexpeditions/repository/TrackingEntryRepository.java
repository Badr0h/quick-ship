package com.quickship.serviceexpeditions.repository;
import com.quickship.serviceexpeditions.entite.TrackingEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TrackingEntryRepository extends JpaRepository<TrackingEntry, Long> {
    List<TrackingEntry> findByShipmentIdOrderByDateAsc(Long shipmentId);
}
