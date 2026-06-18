package com.fibre.optique.subscription.dto;

import com.fibre.optique.subscription.entity.Contrat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ContratDto {

    private Long id;
    private Long abonnementId;
    private String pdfStorageKey;
    private Instant dateSignature;

    public static ContratDto fromEntity(Contrat contrat) {
        return ContratDto.builder()
                .id(contrat.getId())
                .abonnementId(contrat.getAbonnement().getId())
                .pdfStorageKey(contrat.getPdfStorageKey())
                .dateSignature(contrat.getDateSignature())
                .build();
    }
}
