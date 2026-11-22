package com.quickship.serviceexpeditions.entite;

import com.quickship.serviceexpeditions.enums.StatutShipment;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "shipment")
public class Shipment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long customerId;
    private String destinataireNom;
    private String adresseDestinataire;
    private Double poids;
    private Long routeId;
    private BigDecimal montantTransport;
    private String numeroSuivi;
    @Enumerated(EnumType.STRING)
    private StatutShipment statut;
    @Column(unique = true, length = 255)
    private String idempotencyKey;
    private boolean capaciteReservee;
    private boolean capaciteLiberee;
    private boolean encoursApplique;

    public Shipment() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getDestinataireNom() { return destinataireNom; }
    public void setDestinataireNom(String destinataireNom) { this.destinataireNom = destinataireNom; }
    public String getAdresseDestinataire() { return adresseDestinataire; }
    public void setAdresseDestinataire(String adresseDestinataire) { this.adresseDestinataire = adresseDestinataire; }
    public Double getPoids() { return poids; }
    public void setPoids(Double poids) { this.poids = poids; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public BigDecimal getMontantTransport() { return montantTransport; }
    public void setMontantTransport(BigDecimal montantTransport) { this.montantTransport = montantTransport; }
    public String getNumeroSuivi() { return numeroSuivi; }
    public void setNumeroSuivi(String numeroSuivi) { this.numeroSuivi = numeroSuivi; }
    public StatutShipment getStatut() { return statut; }
    public void setStatut(StatutShipment statut) { this.statut = statut; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public boolean isCapaciteReservee() { return capaciteReservee; }
    public void setCapaciteReservee(boolean capaciteReservee) { this.capaciteReservee = capaciteReservee; }
    public boolean isCapaciteLiberee() { return capaciteLiberee; }
    public void setCapaciteLiberee(boolean capaciteLiberee) { this.capaciteLiberee = capaciteLiberee; }
    public boolean isEncoursApplique() { return encoursApplique; }
    public void setEncoursApplique(boolean encoursApplique) { this.encoursApplique = encoursApplique; }
}
