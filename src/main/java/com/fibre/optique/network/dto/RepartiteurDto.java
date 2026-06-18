package com.fibre.optique.network.dto;

import com.fibre.optique.network.entity.Repartiteur;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RepartiteurDto {

    private Long id;
    private String nom;
    private Integer nbPorts;
    private Double longitude;
    private Double latitude;
    private Long datacenterId;
    private String datacenterNom;

    public static RepartiteurDto fromEntity(Repartiteur rep) {
        if (rep == null) return null;
        return RepartiteurDto.builder()
                .id(rep.getId())
                .nom(rep.getNom())
                .nbPorts(rep.getNbPorts())
                .longitude(rep.getCoordinate() != null ? rep.getCoordinate().getX() : null)
                .latitude(rep.getCoordinate() != null ? rep.getCoordinate().getY() : null)
                .datacenterId(rep.getDatacenter().getId())
                .datacenterNom(rep.getDatacenter().getNom())
                .build();
    }
}
