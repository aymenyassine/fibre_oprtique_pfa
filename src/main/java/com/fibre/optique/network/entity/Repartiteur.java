package com.fibre.optique.network.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "repartiteur")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Repartiteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(name = "nb_ports", nullable = false)
    private Integer nbPorts;

    @Column(nullable = false, columnDefinition = "POINT SRID 4326")
    private Point coordinate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "datacenter_id", nullable = false)
    private Datacenter datacenter;
}
