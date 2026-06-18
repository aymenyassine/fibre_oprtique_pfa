package com.fibre.optique.network.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RepartiteurRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotNull(message = "Le nombre de ports est obligatoire")
    @Min(value = 1, message = "Le nombre de ports doit être au moins 1")
    private Integer nbPorts;

    @NotNull(message = "La longitude est obligatoire")
    @DecimalMin(value = "-180.0", message = "Longitude invalide")
    @DecimalMax(value = "180.0",  message = "Longitude invalide")
    private Double longitude;

    @NotNull(message = "La latitude est obligatoire")
    @DecimalMin(value = "-90.0", message = "Latitude invalide")
    @DecimalMax(value = "90.0",  message = "Latitude invalide")
    private Double latitude;

    @NotNull(message = "L'identifiant du datacenter est obligatoire")
    private Long datacenterId;
}
