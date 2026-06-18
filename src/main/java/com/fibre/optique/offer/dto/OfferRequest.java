package com.fibre.optique.offer.dto;

import com.fibre.optique.offer.entity.Technologie;
import com.fibre.optique.offer.entity.TypeEngagement;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OfferRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String nom;

    private String description;

    @NotNull(message = "Le débit montant est obligatoire")
    @Min(value = 1, message = "Le débit montant doit être au moins 1 Mbps")
    private Integer debitMontant;

    @NotNull(message = "Le débit descendant est obligatoire")
    @Min(value = 1, message = "Le débit descendant doit être au moins 1 Mbps")
    private Integer debitDescendant;

    @NotNull(message = "Le prix HT est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix HT doit être positif")
    private BigDecimal prixHT;

    @DecimalMin(value = "0.00", message = "Le taux TVA ne peut pas être négatif")
    @DecimalMax(value = "100.00", message = "Le taux TVA ne peut pas dépasser 100%")
    private BigDecimal tauxTVA;

    @NotNull(message = "Le type d'engagement est obligatoire")
    private TypeEngagement typeEngagement;

    @Min(value = 0, message = "La durée ne peut pas être négative")
    private Integer dureeMois;

    @NotNull(message = "La technologie est obligatoire")
    private Technologie technologie;

    private Boolean actif;
}
