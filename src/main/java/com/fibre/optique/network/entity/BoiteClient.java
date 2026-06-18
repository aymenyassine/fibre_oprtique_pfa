package com.fibre.optique.network.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "boite_client")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoiteClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(name = "nb_ports", nullable = false)
    private Integer nbPorts;

    @Column(name = "ports_utilises", nullable = false)
    @Builder.Default
    private Integer portsUtilises = 0;

    @Column(nullable = false, columnDefinition = "POINT SRID 4326")
    private Point coordinate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "splitter_id", nullable = false)
    private Splitter splitter;
}
