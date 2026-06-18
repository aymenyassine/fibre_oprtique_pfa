package com.fibre.optique.network.dto;

import com.fibre.optique.network.entity.Splitter;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SplitterDto {

    private Long id;
    private String ratio;
    private Integer nbSortie;
    private Long repartiteurId;
    private String repartiteurNom;

    public static SplitterDto fromEntity(Splitter s) {
        return SplitterDto.builder()
                .id(s.getId())
                .ratio(s.getRatio())
                .nbSortie(s.getNbSortie())
                .repartiteurId(s.getRepartiteur().getId())
                .repartiteurNom(s.getRepartiteur().getNom())
                .build();
    }
}
