package com.fibre.optique.subscription.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeOfferRequest {

    @NotNull(message = "L'identifiant de la nouvelle offre est obligatoire")
    private Long newOffreId;
}
