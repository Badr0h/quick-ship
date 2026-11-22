package com.quickship.serviceclients.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

public class CustomerDto implements Serializable {
    private final Long id;
    @NotBlank @Size(max = 255) private final String nom;
    @NotBlank @Email @Size(max = 255) private final String email;
    @NotNull @DecimalMin("0") private final BigDecimal encours;
    @NotNull @DecimalMin("0") private final BigDecimal plafondCredit;
    @NotBlank private final String statut;

    @JsonCreator
    public CustomerDto(@JsonProperty("id") Long id,
                       @JsonProperty("nom") String nom,
                       @JsonProperty("email") String email,
                       @JsonProperty("encours") BigDecimal encours,
                       @JsonProperty("plafondCredit") BigDecimal plafondCredit,
                       @JsonProperty("statut") String statut) {
        this.id = id; this.nom = nom; this.email = email;
        this.encours = encours; this.plafondCredit = plafondCredit; this.statut = statut;
    }
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getEmail() { return email; }
    public BigDecimal getEncours() { return encours; }
    public BigDecimal getPlafondCredit() { return plafondCredit; }
    public String getStatut() { return statut; }
}
