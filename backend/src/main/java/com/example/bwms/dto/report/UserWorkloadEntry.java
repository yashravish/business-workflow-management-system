// Translated from: backend/app/schemas/report.py (UserWorkloadEntry)
package com.example.bwms.dto.report;

import com.example.bwms.model.UserRole;

public record UserWorkloadEntry(
        Long userId,
        String name,
        String email,
        UserRole role,
        int total,
        int pending,
        int inProgress,
        int submitted,
        int approved,
        int rejected,
        int completed
) {}
