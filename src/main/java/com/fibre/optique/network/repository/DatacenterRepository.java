package com.fibre.optique.network.repository;

import com.fibre.optique.network.entity.Datacenter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DatacenterRepository extends JpaRepository<Datacenter, Long> {

    boolean existsByNomIgnoreCase(String nom);
    
    Optional<Datacenter> findByNomIgnoreCase(String nom);
}
