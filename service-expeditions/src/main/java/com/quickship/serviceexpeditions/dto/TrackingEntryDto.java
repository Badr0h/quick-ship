package com.quickship.serviceexpeditions.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class TrackingEntryDto {
    private final Long id;
    @NotNull private final Long shipmentId;
    private final LocalDateTime date;
    @NotBlank private final String localisation;
    private final String commentaire;

    @JsonCreator
    public TrackingEntryDto(@JsonProperty("id") Long id,
                            @JsonProperty("shipmentId") Long shipmentId,
                            @JsonProperty("date") LocalDateTime date,
                            @JsonProperty("localisation") String localisation,
                            @JsonProperty("commentaire") String commentaire) {
        this.id = id; this.shipmentId = shipmentId; this.date = date;
        this.localisation = localisation; this.commentaire = commentaire;
    }
    public Long getId() { return id; }
    public Long getShipmentId() { return shipmentId; }
    public LocalDateTime getDate() { return date; }
    public String getLocalisation() { return localisation; }
    public String getCommentaire() { return commentaire; }
}
