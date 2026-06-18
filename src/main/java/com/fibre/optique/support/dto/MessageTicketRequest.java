package com.fibre.optique.support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageTicketRequest {

    @NotBlank(message = "Le message ne peut pas être vide")
    private String message;
}
