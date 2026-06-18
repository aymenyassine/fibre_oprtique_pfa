package com.fibre.optique.request.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Submitted by a prospect (unauthenticated or authenticated as PROSPECT).
 * latitude/longitude are optional — if provided, a real geospatial eligibility
 * check is performed. Otherwise the request is saved as SOUMISE for manual
 * review by a commercial agent.
 */
@Data
public class DemandeRaccordementRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String prospectNom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String prospectPrenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 150)
    private String prospectEmail;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^[+0-9 \\-().]{6,20}$", message = "Numéro de téléphone invalide")
    private String prospectTelephone;

    @NotBlank(message = "L'adresse de raccordement est obligatoire")
    private String adresseRaccordement;

    /** WGS84 longitude — optional. Required for automatic eligibility check. */
    @DecimalMin(value = "-180.0", message = "Longitude invalide")
    @DecimalMax(value = "180.0",  message = "Longitude invalide")
    private Double longitude;

    /** WGS84 latitude — optional. Required for automatic eligibility check. */
    @DecimalMin(value = "-90.0", message = "Latitude invalide")
    @DecimalMax(value = "90.0",  message = "Latitude invalide")
    private Double latitude;
}
