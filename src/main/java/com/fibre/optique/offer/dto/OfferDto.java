package com.fibre.optique.offer.dto;

import com.fibre.optique.offer.entity.Offre;
import com.fibre.optique.offer.entity.Technologie;
import com.fibre.optique.offer.entity.TypeEngagement;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OfferDto {

    private Long id;
    private String nom;
    private String description;
    private Integer debitMontant;
    private Integer debitDescendant;
    private BigDecimal prixHT;
    private BigDecimal tauxTVA;
    /** Prix TTC calculé = prixHT * (1 + tauxTVA / 100) */
    private BigDecimal prixTTC;
    private TypeEngagement typeEngagement;
    private Integer dureeMois;
    private Technologie technologie;
    private Boolean actif;

    public static OfferDto fromEntity(Offre offre) {
        BigDecimal tva = offre.getTauxTVA() != null ? offre.getTauxTVA() : BigDecimal.ZERO;
        BigDecimal prixTTC = offre.getPrixHT()
                .multiply(BigDecimal.ONE.add(tva.divide(new BigDecimal("100"))));

        return OfferDto.builder()
                .id(offre.getId())
                .nom(offre.getNom())
                .description(offre.getDescription())
                .debitMontant(offre.getDebitMontant())
                .debitDescendant(offre.getDebitDescendant())
                .prixHT(offre.getPrixHT())
                .tauxTVA(tva)
                .prixTTC(prixTTC.setScale(2, java.math.RoundingMode.HALF_UP))
                .typeEngagement(offre.getTypeEngagement())
                .dureeMois(offre.getDureeMois())
                .technologie(offre.getTechnologie())
                .actif(offre.getActif())
                .build();
    }
}
