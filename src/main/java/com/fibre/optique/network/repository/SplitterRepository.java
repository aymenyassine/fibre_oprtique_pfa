package com.fibre.optique.network.repository;

import com.fibre.optique.network.entity.Splitter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SplitterRepository extends JpaRepository<Splitter, Long> {

    List<Splitter> findByRepartiteurId(Long repartiteurId);
    
    List<Splitter> findByRatio(String ratio);
}
