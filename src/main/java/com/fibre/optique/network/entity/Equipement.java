package com.fibre.optique.network.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "equipement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Equipement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String modele;

    @Column(name = "num_serie", nullable = false, unique = true, length = 100)
    private String numSerie;

    @Column(nullable = false, length = 45)
    private String ip;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, length = 20)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartiteur_id", nullable = false)
    private Repartiteur repartiteur;
}
