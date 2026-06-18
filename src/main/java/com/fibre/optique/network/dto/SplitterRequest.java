package com.fibre.optique.network.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SplitterRequest {

    /** Ratio de division ex: "1:32", "1:64" */
    @NotBlank(message = "Le ratio est obligatoire (ex: 1:32, 1:64)")
    @Pattern(regexp = "1:(2|4|8|16|32|64|128)", message = "Format ratio invalide. Exemples: 1:32, 1:64")
    private String ratio;

    @NotNull(message = "Le nombre de sorties est obligatoire")
    @Min(value = 1, message = "Le nombre de sorties doit être au moins 1")
    private Integer nbSortie;

    @NotNull(message = "L'identifiant du répartiteur est obligatoire")
    private Long repartiteurId;
}
