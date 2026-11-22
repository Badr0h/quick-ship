package com.quickship.servicereseau.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.io.Serializable;

public class DepotDto implements Serializable {
    private final Long id;
    @NotBlank private final String nom;
    @NotBlank private final String ville;
    @NotNull @Min(1) private final Integer capacite;

    @JsonCreator
    public DepotDto(@JsonProperty("id") Long id, @JsonProperty("nom") String nom,
                    @JsonProperty("ville") String ville, @JsonProperty("capacite") Integer capacite) {
        this.id = id; this.nom = nom; this.ville = ville; this.capacite = capacite;
    }
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getVille() { return ville; }
    public Integer getCapacite() { return capacite; }
}
