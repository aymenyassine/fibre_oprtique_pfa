package com.fibre.optique.network.dto;

import com.fibre.optique.network.entity.CheminFibre;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheminFibreDto {

    private Long id;
    private Long sourceNodeId;
    private Long destNodeId;
    private Double longueur;
    private String typeFibre;
    private String statut;

    public static CheminFibreDto fromEntity(CheminFibre c) {
        return CheminFibreDto.builder()
                .id(c.getId())
                .sourceNodeId(c.getSourceNodeId())
                .destNodeId(c.getDestNodeId())
                .longueur(c.getLongueur())
                .typeFibre(c.getTypeFibre())
                .statut(c.getStatut())
                .build();
    }
}
