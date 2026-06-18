package com.fibre.optique.request.dto;

import com.fibre.optique.request.entity.DemandeRaccordement;
import com.fibre.optique.request.entity.DemandeStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class DemandeRaccordementDto {

    private Long id;
    private String prospectNom;
    private String prospectPrenom;
    private String prospectEmail;
    private String prospectTelephone;
    private String adresseRaccordement;
    private DemandeStatus statut;
    private String technologieDisponible;
    private BigDecimal montantDevis;
    private Instant datePlanification;
    private Long technicienId;
    private String technicienNom;
    private Instant createdAt;
    private Long clientCreatedId;
    private Long offreIdChoisie;

    public static DemandeRaccordementDto fromEntity(DemandeRaccordement d) {
        return DemandeRaccordementDto.builder()
                .id(d.getId())
                .prospectNom(d.getProspectNom())
                .prospectPrenom(d.getProspectPrenom())
                .prospectEmail(d.getProspectEmail())
                .prospectTelephone(d.getProspectTelephone())
                .adresseRaccordement(d.getAdresseRaccordement())
                .statut(d.getStatut())
                .technologieDisponible(d.getTechnologieDisponible())
                .montantDevis(d.getMontantDevis())
                .datePlanification(d.getDatePlanification())
                .technicienId(d.getTechnicien() != null ? d.getTechnicien().getId() : null)
                .technicienNom(d.getTechnicien() != null
                        ? d.getTechnicien().getNom() + " " + d.getTechnicien().getPrenom()
                        : null)
                .createdAt(d.getCreatedAt())
                .clientCreatedId(d.getClientCreatedId())
                .offreIdChoisie(d.getOffreIdChoisie())
                .build();
    }
}
