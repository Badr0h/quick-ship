package com.quickship.serviceexpeditions.feign;
import java.math.BigDecimal;
public class CustomerDto {
    private Long id; private String nom; private String email;
    private BigDecimal encours; private BigDecimal plafondCredit; private String statut;
    public CustomerDto() {}
    public CustomerDto(Long id, String nom, String email, BigDecimal encours, BigDecimal plafondCredit, String statut) {
        this.id = id; this.nom = nom; this.email = email; this.encours = encours;
        this.plafondCredit = plafondCredit; this.statut = statut;
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; } public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public BigDecimal getEncours() { return encours; } public void setEncours(BigDecimal encours) { this.encours = encours; }
    public BigDecimal getPlafondCredit() { return plafondCredit; } public void setPlafondCredit(BigDecimal plafondCredit) { this.plafondCredit = plafondCredit; }
    public String getStatut() { return statut; } public void setStatut(String statut) { this.statut = statut; }
}
