package com.quickship.servicereseau.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;

public class RouteDto implements Serializable {
    private final Long id;
    @NotBlank private final String nom;
    @NotBlank private final String villeDepart;
    @NotBlank private final String villeArrivee;
    @NotNull private final LocalDate dateDepart;
    @NotNull @Min(1) private final Integer dureeEstimee;
    @NotNull @Min(1) private final Integer capaciteColis;
    private final Integer capaciteReservee;

    @JsonCreator
    public RouteDto(@JsonProperty("id") Long id, @JsonProperty("nom") String nom,
                    @JsonProperty("villeDepart") String villeDepart,
                    @JsonProperty("villeArrivee") String villeArrivee,
                    @JsonProperty("dateDepart") LocalDate dateDepart,
                    @JsonProperty("dureeEstimee") Integer dureeEstimee,
                    @JsonProperty("capaciteColis") Integer capaciteColis,
                    @JsonProperty("capaciteReservee") Integer capaciteReservee) {
        this.id = id; this.nom = nom; this.villeDepart = villeDepart; this.villeArrivee = villeArrivee;
        this.dateDepart = dateDepart; this.dureeEstimee = dureeEstimee;
        this.capaciteColis = capaciteColis; this.capaciteReservee = capaciteReservee;
    }
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getVilleDepart() { return villeDepart; }
    public String getVilleArrivee() { return villeArrivee; }
    public LocalDate getDateDepart() { return dateDepart; }
    public Integer getDureeEstimee() { return dureeEstimee; }
    public Integer getCapaciteColis() { return capaciteColis; }
    public Integer getCapaciteReservee() { return capaciteReservee; }
}
