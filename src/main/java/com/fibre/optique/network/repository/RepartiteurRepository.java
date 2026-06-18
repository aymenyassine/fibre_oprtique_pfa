package com.fibre.optique.network.repository;

import com.fibre.optique.network.entity.Repartiteur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepartiteurRepository extends JpaRepository<Repartiteur, Long> {

    List<Repartiteur> findByDatacenterId(Long datacenterId);

    boolean existsByNomIgnoreCase(String nom);
}
