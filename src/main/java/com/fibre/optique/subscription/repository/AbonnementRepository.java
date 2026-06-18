package com.fibre.optique.subscription.repository;

import com.fibre.optique.subscription.entity.Abonnement;
import com.fibre.optique.subscription.entity.AbonnementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {

    List<Abonnement> findByClientId(Long clientId);

    List<Abonnement> findByClientIdAndStatut(Long clientId, AbonnementStatus statut);

    /** Used by BillingScheduler to generate monthly invoices. */
    List<Abonnement> findByStatut(AbonnementStatus statut);

    /** Ensures a client doesn't already have an active subscription. */
    boolean existsByClientIdAndStatut(Long clientId, AbonnementStatus statut);

    /** Fetch with client and offer eagerly for billing / PDF generation. */
    @Query("SELECT a FROM Abonnement a JOIN FETCH a.client JOIN FETCH a.offre WHERE a.id = :id")
    Optional<Abonnement> findByIdWithDetails(@Param("id") Long id);
}
