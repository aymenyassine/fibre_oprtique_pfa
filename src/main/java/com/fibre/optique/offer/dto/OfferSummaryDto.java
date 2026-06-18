package com.fibre.optique.offer.dto;

import com.fibre.optique.offer.entity.Offre;
import com.fibre.optique.offer.entity.Technologie;
import com.fibre.optique.offer.entity.TypeEngagement;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Lightweight projection used in lists and cross-module references
 * (e.g. SubscriptionService looking up offer details).
 */
@Data
@Builder
public class OfferSummaryDto {

    private Long id;
    private String nom;
    private Integer debitDescendant;
    private BigDecimal prixHT;
    private BigDecimal prixTTC;
    private TypeEngagement typeEngagement;
    private Technologie technologie;

    public static OfferSummaryDto fromEntity(Offre offre) {
        BigDecimal tva = offre.getTauxTVA() != null ? offre.getTauxTVA() : BigDecimal.ZERO;
        BigDecimal prixTTC = offre.getPrixHT()
                .multiply(BigDecimal.ONE.add(tva.divide(new BigDecimal("100"))));

        return OfferSummaryDto.builder()
                .id(offre.getId())
                .nom(offre.getNom())
                .debitDescendant(offre.getDebitDescendant())
                .prixHT(offre.getPrixHT())
                .prixTTC(prixTTC.setScale(2, java.math.RoundingMode.HALF_UP))
                .typeEngagement(offre.getTypeEngagement())
                .technologie(offre.getTechnologie())
                .build();
    }
}
