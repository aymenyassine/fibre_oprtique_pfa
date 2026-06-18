package com.fibre.optique.notification.dto;

import com.fibre.optique.notification.entity.NotificationChannel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Manual notification trigger — admin use only. */
@Data
public class SendNotificationRequest {

    @NotBlank(message = "Le destinataire est obligatoire")
    @Email(message = "Format d'email invalide")
    private String destinataire;

    @NotBlank(message = "Le sujet est obligatoire")
    private String sujet;

    @NotBlank(message = "Le contenu est obligatoire")
    private String contenu;

    @NotNull(message = "Le canal est obligatoire")
    private NotificationChannel typeCanal;
}
