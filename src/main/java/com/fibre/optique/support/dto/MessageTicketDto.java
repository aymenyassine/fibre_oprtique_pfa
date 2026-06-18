package com.fibre.optique.support.dto;

import com.fibre.optique.support.entity.MessageTicket;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MessageTicketDto {

    private Long id;
    private Long ticketId;
    private Long auteurId;
    private String auteurNom;
    private String auteurEmail;
    private String message;
    private Instant dateEnvoi;

    public static MessageTicketDto fromEntity(MessageTicket m) {
        return MessageTicketDto.builder()
                .id(m.getId())
                .ticketId(m.getTicket().getId())
                .auteurId(m.getAuteur().getId())
                .auteurNom(m.getAuteur().getNom() + " " + m.getAuteur().getPrenom())
                .auteurEmail(m.getAuteur().getEmail())
                .message(m.getMessage())
                .dateEnvoi(m.getDateEnvoi())
                .build();
    }
}
