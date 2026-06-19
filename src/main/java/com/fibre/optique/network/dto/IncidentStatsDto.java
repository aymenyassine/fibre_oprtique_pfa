package com.fibre.optique.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentStatsDto {
    private String date; // ISO date "yyyy-MM-dd"
    private long newIncidents;
    private long resolved;
}
