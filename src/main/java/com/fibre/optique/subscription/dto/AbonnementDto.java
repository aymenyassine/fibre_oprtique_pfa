package com.fibre.optique.subscription.dto;

import com.fibre.optique.offer.dto.OfferSummaryDto;
import com.fibre.optique.subscription.entity.Abonnement;
import com.fibre.optique.subscription.entity.AbonnementStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class AbonnementDto {

    private Long id;
    private Long clientId;
    private String clientNom;
    private String clientPrenom;
    private String clientEmail;
    private OfferSummaryDto offre;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private AbonnementStatus statut;
    private Instant dateChangementOffre;

    public static AbonnementDto fromEntity(Abonnement a) {
        return AbonnementDto.builder()
                .id(a.getId())
                .clientId(a.getClient().getId())
                .clientNom(a.getClient().getNom())
                .clientPrenom(a.getClient().getPrenom())
                .clientEmail(a.getClient().getEmail())
                .offre(OfferSummaryDto.fromEntity(a.getOffre()))
                .dateDebut(a.getDateDebut())
                .dateFin(a.getDateFin())
                .statut(a.getStatut())
                .dateChangementOffre(a.getDateChangementOffre())
                .build();
    }
}
