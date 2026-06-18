package com.fibre.optique.subscription.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionRequest {

    @NotNull(message = "L'identifiant du client est obligatoire")
    private Long clientId;

    @NotNull(message = "L'identifiant de l'offre est obligatoire")
    private Long offreId;
}
