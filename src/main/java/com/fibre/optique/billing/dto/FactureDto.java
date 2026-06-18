package com.fibre.optique.billing.dto;

import com.fibre.optique.billing.entity.Facture;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class FactureDto {

    private Long id;
    private String reference;
    private Long clientId;
    private String clientNom;
    private String clientEmail;
    private Long abonnementId;
    private String offreNom;
    private LocalDate periodeDebut;
    private LocalDate periodeFin;
    private BigDecimal montantHT;
    private BigDecimal tauxTva;
    private BigDecimal montantTTC;
    private String statut;
    private LocalDate dateEmission;
    private LocalDate dateEcheance;
    private Instant datePaiement;
    private String pdfStorageKey;

    public static FactureDto fromEntity(Facture f) {
        return FactureDto.builder()
                .id(f.getId())
                .reference(f.getReference())
                .clientId(f.getClient().getId())
                .clientNom(f.getClient().getNom() + " " + f.getClient().getPrenom())
                .clientEmail(f.getClient().getEmail())
                .abonnementId(f.getAbonnement().getId())
                .offreNom(f.getAbonnement().getOffre().getNom())
                .periodeDebut(f.getPeriodeDebut())
                .periodeFin(f.getPeriodeFin())
                .montantHT(f.getMontantHT())
                .tauxTva(f.getTauxTva())
                .montantTTC(f.getMontantTTC())
                .statut(f.getStatut() != null ? f.getStatut().name() : null)
                .dateEmission(f.getDateEmission())
                .dateEcheance(f.getDateEcheance())
                .datePaiement(f.getDatePaiement())
                .pdfStorageKey(f.getPdfStorageKey())
                .build();
    }
}
