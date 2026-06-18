package com.fibre.optique.billing.repository;

import com.fibre.optique.billing.entity.Facture;
import com.fibre.optique.billing.entity.FactureStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FactureRepository extends JpaRepository<Facture, Long> {

    List<Facture> findByClientId(Long clientId);

    List<Facture> findByClientIdAndStatut(Long clientId, FactureStatus statut);

    List<Facture> findByAbonnementId(Long abonnementId);

    /** Used by BillingScheduler to find overdue invoices. */
    List<Facture> findByStatutAndDateEcheanceBefore(FactureStatus statut, LocalDate date);

    /** Used by BillingScheduler — avoid double-billing the same subscription in the same month. */
    boolean existsByAbonnementIdAndDateEmissionBetween(
            Long abonnementId, LocalDate start, LocalDate end);

    /** Paginated search for admin invoice list. */
    @Query("""
            SELECT f FROM Facture f
            WHERE (:clientId IS NULL OR f.client.id  = :clientId)
              AND (:statut   IS NULL OR f.statut      = :statut)
              AND (:from     IS NULL OR f.dateEmission >= :from)
              AND (:to       IS NULL OR f.dateEmission <= :to)
            ORDER BY f.dateEmission DESC
            """)
    Page<Facture> search(
            @Param("clientId") Long clientId,
            @Param("statut")   FactureStatus statut,
            @Param("from")     LocalDate from,
            @Param("to")       LocalDate to,
            Pageable pageable
    );

    /** Next sequence number for the current year — used to build the FAC-YYYY-NNNN reference. */
    @Query("SELECT COUNT(f) FROM Facture f WHERE YEAR(f.dateEmission) = :year")
    long countByYear(@Param("year") int year);
}
