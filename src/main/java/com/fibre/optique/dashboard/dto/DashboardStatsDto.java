package com.fibre.optique.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private long activeOffersCount;
    private long clientsCount;
    private long openTicketsCount;
    private long networkIncidentsCount;
}
