package com.fibre.optique.request.repository;

import com.fibre.optique.request.entity.DemandeRaccordement;
import com.fibre.optique.request.entity.DemandeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DemandeRaccordementRepository extends JpaRepository<DemandeRaccordement, Long> {

    List<DemandeRaccordement> findByStatut(DemandeStatus statut);

    List<DemandeRaccordement> findByTechnicienId(Long technicienId);

    List<DemandeRaccordement> findByProspectEmailIgnoreCase(String email);

    @Query("""
            SELECT d FROM DemandeRaccordement d
            WHERE (:statut  IS NULL OR d.statut = :statut)
              AND (:keyword IS NULL
                   OR LOWER(d.prospectNom)    LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(d.prospectPrenom) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(d.prospectEmail)  LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(d.adresseRaccordement) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY d.createdAt DESC
            """)
    Page<DemandeRaccordement> search(
            @Param("statut")  DemandeStatus statut,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
