package com.quickship.serviceexpeditions.feign;
import java.math.BigDecimal;
public class EncoursUpdateDto {
    private BigDecimal montant;
    public EncoursUpdateDto() {}
    public EncoursUpdateDto(BigDecimal montant) { this.montant = montant; }
    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }
}
