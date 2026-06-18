package com.fibre.optique.network.repository;

import com.fibre.optique.network.entity.Datacenter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatacenterRepository extends JpaRepository<Datacenter, Long> {

    boolean existsByNomIgnoreCase(String nom);
}
