// Translated from: backend/app/api/routes/audit_logs.py
package com.example.bwms.controller;

import com.example.bwms.dto.AuditLogReadDto;
import com.example.bwms.model.AuditAction;
import com.example.bwms.service.AuditLogService;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    // Python: GET /audit-logs?action=...&limit=...
    @GetMapping
    public List<AuditLogReadDto> listAuditLogs(
            @RequestParam @Nullable AuditAction action,
            @RequestParam(defaultValue = "100") int limit) {
        return auditLogService.listLogs(action, Math.min(Math.max(limit, 1), 500)).stream()
                .map(AuditLogReadDto::from)
                .toList();
    }
}
