package com.quickship.serviceclients.feign;

import java.math.BigDecimal;

public class ShipmentDto {
    private Long id;
    private Long customerId;
    private BigDecimal montantTransport;
    private String statut;
    private String numeroSuivi;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public BigDecimal getMontantTransport() { return montantTransport; }
    public void setMontantTransport(BigDecimal montantTransport) { this.montantTransport = montantTransport; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getNumeroSuivi() { return numeroSuivi; }
    public void setNumeroSuivi(String numeroSuivi) { this.numeroSuivi = numeroSuivi; }
}
