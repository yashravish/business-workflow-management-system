// Translated from: backend/app/schemas/audit_log.py (AuditLogRead)
package com.example.bwms.dto;

import com.example.bwms.model.AuditAction;
import com.example.bwms.model.AuditLog;

import java.time.OffsetDateTime;

public record AuditLogReadDto(
        Long id,
        Long userId,
        UserSummaryDto user,
        AuditAction action,
        String entityType,
        Long entityId,
        String details,
        OffsetDateTime createdAt
) {
    public static AuditLogReadDto from(AuditLog log) {
        UserSummaryDto userDto = log.getUser() != null ? UserSummaryDto.from(log.getUser()) : null;
        return new AuditLogReadDto(
                log.getId(),
                log.getUserId(),
                userDto,
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getDetails(),
                log.getCreatedAt()
        );
    }
}
