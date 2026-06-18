package com.fibre.optique.network.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chemin_fibre")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheminFibre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_node_id", nullable = false)
    private Long sourceNodeId;

    @Column(name = "dest_node_id", nullable = false)
    private Long destNodeId;

    @Column(nullable = false)
    private Double longueur;

    @Column(name = "type_fibre", nullable = false, length = 20)
    private String typeFibre;

    @Column(nullable = false, length = 30)
    private String statut;
}
