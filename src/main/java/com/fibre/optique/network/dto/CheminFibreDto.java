package com.fibre.optique.network.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheminFibreDto {

    private Long id;
    private Long sourceNodeId;
    private String sourceNodeType;
    private String sourceNodeName;
    private Long destNodeId;
    private String destNodeType;
    private String destNodeName;
    private Double longueur;
    private String typeFibre;
    private String statut;
}
