package com.quickship.serviceexpeditions.repository;
import com.quickship.serviceexpeditions.entite.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByIdempotencyKey(String idempotencyKey);
}
