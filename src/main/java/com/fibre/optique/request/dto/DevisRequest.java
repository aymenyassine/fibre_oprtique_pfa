package com.fibre.optique.request.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Used by COMMERCIAL to set the installation quote and the pre-selected offer.
 */
@Data
public class DevisRequest {

    @NotNull(message = "Le montant du devis est obligatoire")
    @DecimalMin(value = "0.00", message = "Le montant ne peut pas être négatif")
    private BigDecimal montantDevis;

    @NotNull(message = "L'identifiant de l'offre choisie est obligatoire")
    private Long offreId;
}
