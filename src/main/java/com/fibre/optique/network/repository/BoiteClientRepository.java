package com.fibre.optique.network.repository;

import com.fibre.optique.network.entity.BoiteClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoiteClientRepository extends JpaRepository<BoiteClient, Long> {

    List<BoiteClient> findBySplitterId(Long splitterId);

    boolean existsByNom(String nom);
    
    Optional<BoiteClient> findByNom(String nom);

    @Query("SELECT bc FROM BoiteClient bc WHERE bc.portsUtilises < bc.nbPorts")
    List<BoiteClient> findAvailableBoxes();

    @Query(value = "SELECT * FROM boite_client bc WHERE bc.ports_utilises < bc.nb_ports ORDER BY ST_Distance(bc.coordinate, ST_GeomFromText(:pointText, 4326)) LIMIT 1", nativeQuery = true)
    Optional<BoiteClient> findNearestAvailableBox(@Param("pointText") String pointText);

    @Query("SELECT COUNT(bc) FROM BoiteClient bc WHERE bc.splitter.id = :splitterId AND bc.portsUtilises < bc.nbPorts")
    long countAvailableBoxesBySplitterId(@Param("splitterId") Long splitterId);
}
