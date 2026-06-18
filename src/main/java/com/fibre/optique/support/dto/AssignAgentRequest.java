package com.fibre.optique.support.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignAgentRequest {

    @NotNull(message = "L'identifiant de l'agent est obligatoire")
    private Long agentId;
}
