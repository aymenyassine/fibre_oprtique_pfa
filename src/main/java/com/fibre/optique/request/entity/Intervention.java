package com.fibre.optique.request.entity;

import com.fibre.optique.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "intervention")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_id", nullable = false)
    private DemandeRaccordement demande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technicien_id", nullable = false)
    private User technicien;

    @Column(name = "scheduled_time", nullable = false)
    private Instant scheduledTime;

    @Column(nullable = false, length = 50)
    private String statut;

    @Column(name = "check_in_time")
    private Instant checkInTime;

    @Column(name = "check_out_time")
    private Instant checkOutTime;

    @Column(name = "latitude_check_in")
    private Double latitudeCheckIn;

    @Column(name = "longitude_check_in")
    private Double longitudeCheckIn;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
