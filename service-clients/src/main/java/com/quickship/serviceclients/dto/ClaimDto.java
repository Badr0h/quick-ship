package com.quickship.serviceclients.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

public class ClaimDto implements Serializable {
    private final Long id;
    @NotNull private final Long customerId;
    @NotNull private final Long shipmentId;
    @NotBlank private final String description;
    private final BigDecimal montant;
    private final String statut;

    @JsonCreator
    public ClaimDto(@JsonProperty("id") Long id,
                    @JsonProperty("customerId") Long customerId,
                    @JsonProperty("shipmentId") Long shipmentId,
                    @JsonProperty("description") String description,
                    @JsonProperty("montant") BigDecimal montant,
                    @JsonProperty("statut") String statut) {
        this.id = id; this.customerId = customerId; this.shipmentId = shipmentId;
        this.description = description; this.montant = montant; this.statut = statut;
    }
    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public Long getShipmentId() { return shipmentId; }
    public String getDescription() { return description; }
    public BigDecimal getMontant() { return montant; }
    public String getStatut() { return statut; }
}
