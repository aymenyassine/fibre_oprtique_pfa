package com.fibre.optique.auth.repository;

import com.fibre.optique.auth.model.LoginAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginAuditLogRepository extends JpaRepository<LoginAuditLog, Long> {
}
