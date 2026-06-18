package com.fibre.optique.billing.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/** High-level billing dashboard stats — returned by GET /api/v1/billing/stats */
@Data
@Builder
public class BillingStatsDto {

    private long totalFactures;
    private long facturesEnAttente;
    private long facturesPayees;
    private long facturesEnRetard;
    private long facturesAnnulees;
    private BigDecimal chiffreAffairesMoisCourant;   // sum TTC PAYEE this month
    private BigDecimal montantImpayeTotal;            // sum TTC EN_ATTENTE + EN_RETARD
}
