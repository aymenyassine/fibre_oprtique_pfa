package com.fibre.optique.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityResponse {

    /** Whether the address coordinates are within range of an available BoiteClient */
    private boolean eligible;

    /** Technology available (FTTH, FTTB) — null if not eligible */
    private String technologieDisponible;

    /** Name of the nearest available BoiteClient — null if not eligible */
    private String boiteClientNom;

    /** Distance in metres to the nearest available BoiteClient */
    private Double distanceMetres;

    /** Human-readable message */
    private String message;
}
