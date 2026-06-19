package com.fibre.optique.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyBillingStatsDto {
    private String month; // e.g. "Jan", "Fév" or "2026-06"
    private BigDecimal revenue;
    private long invoicesCreated;
}
