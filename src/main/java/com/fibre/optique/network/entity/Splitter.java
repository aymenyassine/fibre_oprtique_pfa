package com.fibre.optique.network.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "splitter")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Splitter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String ratio;

    @Column(name = "nb_sortie", nullable = false)
    private Integer nbSortie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartiteur_id", nullable = false)
    private Repartiteur repartiteur;
}
