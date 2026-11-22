package com.quickship.serviceexpeditions.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public class CreateShipmentDto {
    @NotNull private final Long customerId;
    @NotBlank private final String destinataireNom;
    @NotBlank private final String adresseDestinataire;
    @NotNull @DecimalMin("0.1") private final Double poids;
    @NotNull private final Long routeId;

    @JsonCreator
    public CreateShipmentDto(@JsonProperty("customerId") Long customerId,
                             @JsonProperty("destinataireNom") String destinataireNom,
                             @JsonProperty("adresseDestinataire") String adresseDestinataire,
                             @JsonProperty("poids") Double poids,
                             @JsonProperty("routeId") Long routeId) {
        this.customerId = customerId; this.destinataireNom = destinataireNom;
        this.adresseDestinataire = adresseDestinataire; this.poids = poids; this.routeId = routeId;
    }
    public Long getCustomerId() { return customerId; }
    public String getDestinataireNom() { return destinataireNom; }
    public String getAdresseDestinataire() { return adresseDestinataire; }
    public Double getPoids() { return poids; }
    public Long getRouteId() { return routeId; }
}
