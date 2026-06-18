package com.fibre.optique.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "contrat")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "abonnement_id", nullable = false, unique = true)
    private Abonnement abonnement;

    @Column(name = "pdf_storage_key", nullable = false, length = 255)
    private String pdfStorageKey;

    @Column(name = "date_signature", nullable = false)
    @Builder.Default
    private Instant dateSignature = Instant.now();

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;
}
