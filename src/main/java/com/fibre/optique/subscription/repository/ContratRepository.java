package com.fibre.optique.subscription.repository;

import com.fibre.optique.subscription.entity.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContratRepository extends JpaRepository<Contrat, Long> {

    Optional<Contrat> findByAbonnementId(Long abonnementId);

    boolean existsByAbonnementId(Long abonnementId);
}
