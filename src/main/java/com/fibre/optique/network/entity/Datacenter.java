package com.fibre.optique.network.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import java.time.Instant;

@Entity
@Table(name = "datacenter")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Datacenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false)
    private Integer capacite;

    @Column(nullable = false, columnDefinition = "POINT SRID 4326")
    private Point coordinate;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
