package com.fibre.optique.network.dto;

import com.fibre.optique.network.entity.BoiteClient;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BoiteClientDto {

    private Long id;
    private String nom;
    private Integer nbPorts;
    private Integer portsUtilises;
    private Integer portsDisponibles;
    private Double longitude;
    private Double latitude;
    private Long splitterId;
    private String splitterRatio;

    public static BoiteClientDto fromEntity(BoiteClient bc) {
        return BoiteClientDto.builder()
                .id(bc.getId())
                .nom(bc.getNom())
                .nbPorts(bc.getNbPorts())
                .portsUtilises(bc.getPortsUtilises())
                .portsDisponibles(bc.getNbPorts() - bc.getPortsUtilises())
                .longitude(bc.getCoordinate() != null ? bc.getCoordinate().getX() : null)
                .latitude(bc.getCoordinate()  != null ? bc.getCoordinate().getY()  : null)
                .splitterId(bc.getSplitter().getId())
                .splitterRatio(bc.getSplitter().getRatio())
                .build();
    }
}
