package com.fibre.optique.network.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DatacenterRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotNull(message = "La capacité est obligatoire")
    @Min(value = 1, message = "La capacité doit être au moins 1")
    private Integer capacite;

    @NotNull(message = "La longitude est obligatoire")
    @DecimalMin(value = "-180.0", message = "Longitude invalide (entre -180 et 180)")
    @DecimalMax(value = "180.0",  message = "Longitude invalide (entre -180 et 180)")
    private Double longitude;

    @NotNull(message = "La latitude est obligatoire")
    @DecimalMin(value = "-90.0", message = "Latitude invalide (entre -90 et 90)")
    @DecimalMax(value = "90.0",  message = "Latitude invalide (entre -90 et 90)")
    private Double latitude;
}
