package com.quickship.serviceclients.repository;

import com.quickship.serviceclients.entite.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByShipmentId(Long shipmentId);
    Optional<Claim> findByShipmentIdAndStatut(Long shipmentId, String statut);
    boolean existsByShipmentId(Long shipmentId);
}
