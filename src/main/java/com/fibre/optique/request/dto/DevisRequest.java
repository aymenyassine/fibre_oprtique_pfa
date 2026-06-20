package com.fibre.optique.request.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DevisRequest {

    @NotNull(message = "La décision est obligatoire")
    private DevisDecision decision;

    /** Obligatoire quand decision == ANNULE */
    private RaisonAnnulation raisonAnnulation;

    /** Commentaire libre — obligatoire quand decision == ANNULE */
    private String commentaire;
}
