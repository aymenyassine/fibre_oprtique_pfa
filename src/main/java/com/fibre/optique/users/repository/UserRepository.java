package com.fibre.optique.users.repository;

import com.fibre.optique.users.entity.Role;
import com.fibre.optique.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE (:query IS NULL
                   OR LOWER(u.nom)    LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(u.email)  LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:role    IS NULL OR u.role    = :role)
              AND (:enabled IS NULL OR u.enabled = :enabled)
            """)
    Page<User> searchUsers(
            @Param("query")   String query,
            @Param("role")    Role role,
            @Param("enabled") Boolean enabled,
            Pageable pageable
    );
}
