package com.quickship.serviceexpeditions.feign;
import java.time.LocalDate;
public class RouteDto {
    private Long id; private String nom; private String villeDepart; private String villeArrivee;
    private LocalDate dateDepart; private Integer dureeEstimee; private Integer capaciteColis; private Integer capaciteReservee;
    public RouteDto() {}
    public RouteDto(Long id, String nom, String villeDepart, String villeArrivee, LocalDate dateDepart,
                    Integer dureeEstimee, Integer capaciteColis, Integer capaciteReservee) {
        this.id = id; this.nom = nom; this.villeDepart = villeDepart; this.villeArrivee = villeArrivee;
        this.dateDepart = dateDepart; this.dureeEstimee = dureeEstimee;
        this.capaciteColis = capaciteColis; this.capaciteReservee = capaciteReservee;
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; } public void setNom(String nom) { this.nom = nom; }
    public String getVilleDepart() { return villeDepart; } public void setVilleDepart(String villeDepart) { this.villeDepart = villeDepart; }
    public String getVilleArrivee() { return villeArrivee; } public void setVilleArrivee(String villeArrivee) { this.villeArrivee = villeArrivee; }
    public LocalDate getDateDepart() { return dateDepart; } public void setDateDepart(LocalDate dateDepart) { this.dateDepart = dateDepart; }
    public Integer getDureeEstimee() { return dureeEstimee; } public void setDureeEstimee(Integer dureeEstimee) { this.dureeEstimee = dureeEstimee; }
    public Integer getCapaciteColis() { return capaciteColis; } public void setCapaciteColis(Integer capaciteColis) { this.capaciteColis = capaciteColis; }
    public Integer getCapaciteReservee() { return capaciteReservee; } public void setCapaciteReservee(Integer capaciteReservee) { this.capaciteReservee = capaciteReservee; }
}
