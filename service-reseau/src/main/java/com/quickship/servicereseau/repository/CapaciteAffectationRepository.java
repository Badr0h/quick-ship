package com.quickship.servicereseau.repository;

import com.quickship.servicereseau.entite.CapaciteAffectation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CapaciteAffectationRepository extends JpaRepository<CapaciteAffectation, Long> {
    Optional<CapaciteAffectation> findByShipmentId(Long shipmentId);
    boolean existsByShipmentId(Long shipmentId);
}
