package com.fibre.optique.network.dto;

import com.fibre.optique.network.entity.Equipement;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EquipementDto {

    private Long id;
    private String nom;
    private String modele;
    private String numSerie;
    private String ip;
    private String status;
    private String type;
    private Long repartiteurId;
    private String repartiteurNom;

    public static EquipementDto fromEntity(Equipement e) {
        return EquipementDto.builder()
                .id(e.getId())
                .nom(e.getNom())
                .modele(e.getModele())
                .numSerie(e.getNumSerie())
                .ip(e.getIp())
                .status(e.getStatus())
                .type(e.getType())
                .repartiteurId(e.getRepartiteur().getId())
                .repartiteurNom(e.getRepartiteur().getNom())
                .build();
    }
}
