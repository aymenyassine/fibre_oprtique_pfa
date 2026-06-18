package com.fibre.optique.network.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheminFibreRequest {

    @NotNull(message = "L'identifiant du nœud source est obligatoire")
    private Long sourceNodeId;

    @NotNull(message = "L'identifiant du nœud destination est obligatoire")
    private Long destNodeId;

    @NotNull(message = "La longueur est obligatoire")
    @DecimalMin(value = "0.001", message = "La longueur doit être positive")
    private Double longueur;

    @NotBlank(message = "Le type de fibre est obligatoire (ex: MONOMODE, MULTIMODE)")
    private String typeFibre;

    @NotBlank(message = "Le statut est obligatoire (ex: OK, INCIDENT, EN_MAINTENANCE)")
    private String statut;
}
