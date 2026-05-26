// Translated from: backend/app/services/audit_log_service.py
package com.example.bwms.service;

import com.example.bwms.model.AuditAction;
import com.example.bwms.model.AuditLog;
import com.example.bwms.repository.AuditLogRepository;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // Python: log_action(*, user_id, action, entity_type, entity_id, details)
    public AuditLog logAction(@Nullable Long userId, AuditAction action,
                              String entityType, @Nullable Long entityId,
                              @Nullable String details) {
        AuditLog entry = new AuditLog(userId, action, entityType, entityId, details);
        auditLogRepository.save(entry);
        log.info("audit user_id={} action={} entity_type={} entity_id={}",
                userId, action.getValue(), entityType, entityId);
        return entry;
    }

    public List<AuditLog> listLogs(@Nullable AuditAction action, int limit) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            if (query.getResultType() == AuditLog.class) {
                root.fetch("user", JoinType.LEFT);
            }
            return action == null ? cb.conjunction() : cb.equal(root.get("action"), action);
        };
        return auditLogRepository.findAll(
                spec,
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
    }
}
