package com.fibre.optique.support.entity;

import com.fibre.optique.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "ticket")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private User agent;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String categorie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TicketPriority priorite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TicketStatus statut;

    @Column(name = "escalation_level", nullable = false)
    @Builder.Default
    private Integer escalationLevel = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant dateCreation = Instant.now();

    @Column(name = "last_activity_at", nullable = false)
    @Builder.Default
    private Instant lastActivityAt = Instant.now();

    @Column(name = "sla_deadline", nullable = false)
    private Instant dateLimiteSla;

    @Column(name = "closed_at")
    private Instant dateResolution;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;
}
