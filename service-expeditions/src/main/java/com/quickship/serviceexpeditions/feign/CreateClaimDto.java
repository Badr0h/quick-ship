package com.quickship.serviceexpeditions.feign;
import java.math.BigDecimal;
public class CreateClaimDto {
    private Long customerId; private Long shipmentId; private String description; private BigDecimal montant; private String statut;
    public CreateClaimDto() {}
    public CreateClaimDto(Long customerId, Long shipmentId, String description) {
        this.customerId = customerId; this.shipmentId = shipmentId; this.description = description;
    }
    public Long getCustomerId() { return customerId; } public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getShipmentId() { return shipmentId; } public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public BigDecimal getMontant() { return montant; } public void setMontant(BigDecimal montant) { this.montant = montant; }
    public String getStatut() { return statut; } public void setStatut(String statut) { this.statut = statut; }
}
