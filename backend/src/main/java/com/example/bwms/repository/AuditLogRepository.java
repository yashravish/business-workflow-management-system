// Translated from: backend/app/repositories/audit_log_repository.py
package com.example.bwms.repository;

import com.example.bwms.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

// Dynamic filter query is built in AuditLogService via Specification; see TaskRepository
// for why the previous "(:action IS NULL OR a.action = :action)" form was unsafe.
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
}
