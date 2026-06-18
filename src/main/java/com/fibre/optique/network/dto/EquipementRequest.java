package com.fibre.optique.network.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EquipementRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;

    @NotBlank(message = "Le modèle est obligatoire")
    @Size(max = 100)
    private String modele;

    @NotBlank(message = "Le numéro de série est obligatoire")
    @Size(max = 100)
    private String numSerie;

    @NotBlank(message = "L'adresse IP est obligatoire")
    @Pattern(regexp = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$",
             message = "Format d'adresse IP invalide")
    @Size(max = 45)
    private String ip;

    @NotBlank(message = "Le statut est obligatoire (ACTIF, PANNE, MAINTENANCE)")
    private String status;

    @NotBlank(message = "Le type est obligatoire (ex: OLT, ONU, SWITCH)")
    private String type;

    @NotNull(message = "L'identifiant du répartiteur est obligatoire")
    private Long repartiteurId;
}
