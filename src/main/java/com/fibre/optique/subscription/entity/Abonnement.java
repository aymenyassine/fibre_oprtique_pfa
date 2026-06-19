package com.fibre.optique.subscription.entity;

import com.fibre.optique.offer.entity.Offre;
import com.fibre.optique.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "abonnement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offre_id", nullable = false)
    private Offre offre;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AbonnementStatus statut;

    @Column(name = "date_changement_offre")
    private Instant dateChangementOffre;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;


    public static LocalDate calculateEndDateFromOffer(Offre offre, LocalDate dateDebut) {
        if (offre == null || offre.getTypeEngagement() == null) {
            return dateDebut.plusMonths(1);
        }
        
        return switch (offre.getTypeEngagement()) {
            case SANS_ENGAGEMENT -> dateDebut.plusMonths(1);
            case DOUZE_MOIS -> dateDebut.plusMonths(12);
            case VINGT_QUATRE_MOIS -> dateDebut.plusMonths(24);
            default -> dateDebut.plusMonths(1);
        };
    }

    @PrePersist
    @PreUpdate
    private void calculateDateFin() {
        if (dateDebut == null) {
            dateDebut = LocalDate.now();
        }
        
        if (statut == AbonnementStatus.ACTIF && dateFin == null) {
            dateFin = Abonnement.calculateEndDateFromOffer(offre, dateDebut);
        }
    }
}
