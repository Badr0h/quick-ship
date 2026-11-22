package com.quickship.servicereseau.entite;

import jakarta.persistence.*;

/**
 * Suit la réservation d'1 unité de capacité pour un colis (fiche V7 :
 * libérer une seule fois, uniquement si réservé).
 */
@Entity
@Table(name = "capacite_affectation",
        uniqueConstraints = @UniqueConstraint(columnNames = "shipmentId"))
public class CapaciteAffectation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private Long shipmentId;
    @Column(nullable = false)
    private Long routeId;
    @Column(nullable = false)
    private boolean liberee;

    public CapaciteAffectation() {}

    public CapaciteAffectation(Long shipmentId, Long routeId) {
        this.shipmentId = shipmentId;
        this.routeId = routeId;
        this.liberee = false;
    }

    public Long getId() { return id; }
    public Long getShipmentId() { return shipmentId; }
    public Long getRouteId() { return routeId; }
    public boolean isLiberee() { return liberee; }
    public void setLiberee(boolean liberee) { this.liberee = liberee; }
}
