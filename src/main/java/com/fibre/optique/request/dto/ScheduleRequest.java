package com.fibre.optique.request.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

/**
 * Used by ADMIN / COMMERCIAL to assign a technician and a planned date.
 */
@Data
public class ScheduleRequest {

    @NotNull(message = "L'identifiant du technicien est obligatoire")
    private Long technicienId;

    @NotNull(message = "La date de planification est obligatoire")
    @Future(message = "La date de planification doit être dans le futur")
    private Instant datePlanification;
}
