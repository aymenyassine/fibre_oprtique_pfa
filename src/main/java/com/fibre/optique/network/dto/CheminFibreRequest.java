package com.fibre.optique.network.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheminFibreRequest {

    // Option 1: Direct IDs (legacy support)
    private Long sourceNodeId;
    private Long destNodeId;

    // Option 2: Type + Name (NEW - more user-friendly)
    private String sourceNodeType;  // "DATACENTER", "REPARTITEUR", "SPLITTER", "BOITE_CLIENT", "EQUIPEMENT"
    private String sourceNodeName;
    
    private String destNodeType;
    private String destNodeName;

    @NotNull(message = "La longueur est obligatoire")
    @DecimalMin(value = "0.001", message = "La longueur doit être positive")
    private Double longueur;

    @NotBlank(message = "Le type de fibre est obligatoire (ex: MONOMODE, MULTIMODE)")
    private String typeFibre;

    @NotBlank(message = "Le statut est obligatoire (ex: OK, INCIDENT, EN_MAINTENANCE)")
    private String statut;
}
