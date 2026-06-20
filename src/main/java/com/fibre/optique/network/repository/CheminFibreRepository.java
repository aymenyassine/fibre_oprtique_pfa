package com.fibre.optique.network.repository;

import com.fibre.optique.network.entity.CheminFibre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheminFibreRepository extends JpaRepository<CheminFibre, Long> {

    List<CheminFibre> findByStatut(String statut);

    long countByStatut(String statut);

    List<CheminFibre> findBySourceNodeIdOrDestNodeId(Long sourceNodeId, Long destNodeId);
}
