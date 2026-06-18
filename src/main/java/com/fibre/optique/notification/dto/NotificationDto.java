package com.fibre.optique.notification.dto;

import com.fibre.optique.notification.entity.Notification;
import com.fibre.optique.notification.entity.NotificationChannel;
import com.fibre.optique.notification.entity.NotificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationDto {

    private Long id;
    private String destinataire;
    private String sujet;
    private String contenu;
    private NotificationChannel typeCanal;
    private NotificationStatus statut;
    private Instant createdAt;
    private Instant sentAt;
    private String errorMessage;

    public static NotificationDto fromEntity(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .destinataire(n.getDestinataire())
                .sujet(n.getSujet())
                .contenu(n.getContenu())
                .typeCanal(n.getTypeCanal())
                .statut(n.getStatut())
                .createdAt(n.getCreatedAt())
                .sentAt(n.getSentAt())
                .errorMessage(n.getErrorMessage())
                .build();
    }
}
