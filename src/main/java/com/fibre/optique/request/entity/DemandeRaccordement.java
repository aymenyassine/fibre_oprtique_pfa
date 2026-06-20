package com.fibre.optique.request.entity;

import com.fibre.optique.users.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "demande_raccordement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeRaccordement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prospect_nom", nullable = false, length = 100)
    private String prospectNom;

    @Column(name = "prospect_prenom", nullable = false, length = 100)
    private String prospectPrenom;

    @Column(name = "prospect_email", nullable = false, length = 255)
    private String prospectEmail;

    @Column(name = "prospect_telephone", nullable = false, length = 20)
    private String prospectTelephone;

    @Column(name = "adresse_raccordement", nullable = false, length = 500)
    private String adresseRaccordement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DemandeStatus statut;

    @Column(name = "technologie_disponible", length = 50)
    private String technologieDisponible;

    @Column(name = "montant_devis", precision = 10, scale = 2)
    private BigDecimal montantDevis;

    @Column(name = "offre_id_choisie")
    private Long offreIdChoisie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technicien_id")
    private User technicien;

    @Column(name = "date_planification")
    private Instant datePlanification;

    @Column(name = "client_created_id")
    private Long clientCreatedId;

    @Column(name = "commentaire_traitement", columnDefinition = "TEXT")
    private String commentaireTraitement;

    @Column(name = "raison_annulation", length = 50)
    private String raisonAnnulation;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
