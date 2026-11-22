package com.quickship.serviceclients.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class EncoursUpdateDto {
    @NotNull @DecimalMin("0")
    private final BigDecimal montant;

    @JsonCreator
    public EncoursUpdateDto(@JsonProperty("montant") BigDecimal montant) {
        this.montant = montant;
    }
    public BigDecimal getMontant() { return montant; }
}
