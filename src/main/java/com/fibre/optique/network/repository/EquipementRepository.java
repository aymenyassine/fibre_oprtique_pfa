package com.fibre.optique.network.repository;

import com.fibre.optique.network.entity.Equipement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipementRepository extends JpaRepository<Equipement, Long> {

    List<Equipement> findByRepartiteurId(Long repartiteurId);

    boolean existsByNomIgnoreCase(String nom);
    
    Optional<Equipement> findByNom(String nom);
}
