package com.fibre.optique.network.dto;

import com.fibre.optique.network.entity.Datacenter;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatacenterDto {

    private Long id;
    private String nom;
    private Integer capacite;
    private Double longitude;
    private Double latitude;

    public static DatacenterDto fromEntity(Datacenter dc) {
        if (dc == null) return null;
        return DatacenterDto.builder()
                .id(dc.getId())
                .nom(dc.getNom())
                .capacite(dc.getCapacite())
                .longitude(dc.getCoordinate() != null ? dc.getCoordinate().getX() : null)
                .latitude(dc.getCoordinate() != null ? dc.getCoordinate().getY() : null)
                .build();
    }
}
