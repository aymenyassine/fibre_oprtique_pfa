package com.fibre.optique.network.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NetworkStatusResponse {

    private long totalDatacenters;
    private long totalRepartiteurs;
    private long totalSplitters;
    private long totalEquipements;
    private long totalBoitesClient;
    private long boitesAvecPortsLibres;
    private long totalCheminsFibre;
    private long cheminsEnIncident;

    /** CheminFibre currently in INCIDENT state */
    private List<CheminFibreDto> activeIncidents;
}
