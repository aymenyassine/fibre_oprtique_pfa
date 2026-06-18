package com.fibre.optique.support.dto;

import com.fibre.optique.support.entity.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 150, message = "Le titre ne peut pas dépasser 150 caractères")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotNull(message = "La priorité est obligatoire")
    private TicketPriority priorite;

    /**
     * Optional: staff can open a ticket on behalf of a client.
     * If null, the authenticated user is treated as the client.
     */
    private Long clientId;
}
