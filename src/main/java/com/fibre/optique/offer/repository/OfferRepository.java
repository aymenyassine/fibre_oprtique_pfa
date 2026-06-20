package com.fibre.optique.offer.repository;

import com.fibre.optique.offer.entity.Offre;
import com.fibre.optique.offer.entity.Technologie;
import com.fibre.optique.offer.entity.TypeEngagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offre, Long> {

    /** All active offers — used for public catalogue. */
    List<Offre> findByActifTrue();

    long countByActifTrue();

    /** Active offers filtered by technology. */
    List<Offre> findByActifTrueAndTechnologie(Technologie technologie);

    /** Active offers filtered by engagement type. */
    List<Offre> findByActifTrueAndTypeEngagement(TypeEngagement typeEngagement);

    /** Active offers filtered by both technology and engagement. */
    List<Offre> findByActifTrueAndTechnologieAndTypeEngagement(
            Technologie technologie, TypeEngagement typeEngagement);

    boolean existsByNomIgnoreCase(String nom);

    /**
     * Search active offers by name keyword (for admin use).
     */
    @Query("SELECT o FROM Offre o WHERE o.actif = true AND LOWER(o.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Offre> searchByNom(@Param("keyword") String keyword);
}
