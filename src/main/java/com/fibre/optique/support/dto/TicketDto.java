package com.fibre.optique.support.dto;

import com.fibre.optique.support.entity.Ticket;
import com.fibre.optique.support.entity.TicketPriority;
import com.fibre.optique.support.entity.TicketStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TicketDto {

    private Long id;
    private Long clientId;
    private String clientNom;
    private String clientEmail;
    private String titre;
    private String description;
    private String categorie;
    private TicketStatus statut;
    private TicketPriority priorite;
    private Instant dateCreation;
    private Instant dateLimiteSla;
    private Instant dateResolution;
    private Long agentId;
    private String agentNom;

    public static TicketDto fromEntity(Ticket t) {
        return TicketDto.builder()
                .id(t.getId())
                .clientId(t.getClient().getId())
                .clientNom(t.getClient().getNom() + " " + t.getClient().getPrenom())
                .clientEmail(t.getClient().getEmail())
                .titre(t.getTitre())
                .description(t.getDescription())
                .categorie(t.getCategorie())
                .statut(t.getStatut())
                .priorite(t.getPriorite())
                .dateCreation(t.getDateCreation())
                .dateLimiteSla(t.getDateLimiteSla())
                .dateResolution(t.getDateResolution())
                .agentId(t.getAgent() != null ? t.getAgent().getId() : null)
                .agentNom(t.getAgent() != null
                        ? t.getAgent().getNom() + " " + t.getAgent().getPrenom()
                        : null)
                .build();
    }
}
