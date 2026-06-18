package com.fibre.optique.offer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "offre")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "debit_montant", nullable = false)
    private Integer debitMontant;

    @Column(name = "debit_descendant", nullable = false)
    private Integer debitDescendant;

    @Column(name = "prix_ht", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixHT;

    @Column(name = "taux_tva", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal tauxTVA = new BigDecimal("20.00");

    @Enumerated(EnumType.STRING)
    @Column(name = "type_engagement", nullable = false, length = 50)
    private TypeEngagement typeEngagement;

    @Column(name = "duree_mois", nullable = false)
    @Builder.Default
    private Integer dureeMois = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Technologie technologie;

    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
