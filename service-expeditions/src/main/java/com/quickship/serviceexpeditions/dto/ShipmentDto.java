package com.quickship.serviceexpeditions.dto;

import com.quickship.serviceexpeditions.enums.StatutShipment;
import java.math.BigDecimal;

public class ShipmentDto {
    private Long id;
    private Long customerId;
    private String destinataireNom;
    private String adresseDestinataire;
    private Double poids;
    private Long routeId;
    private BigDecimal montantTransport;
    private String numeroSuivi;
    private StatutShipment statut;

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
}
