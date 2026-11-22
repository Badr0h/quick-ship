package com.quickship.serviceclients.entite;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String email;
    private BigDecimal encours;
    private BigDecimal plafondCredit;
    private String statut;

    public Customer() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public BigDecimal getEncours() { return encours; }
    public void setEncours(BigDecimal encours) { this.encours = encours; }
    public BigDecimal getPlafondCredit() { return plafondCredit; }
    public void setPlafondCredit(BigDecimal plafondCredit) { this.plafondCredit = plafondCredit; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
