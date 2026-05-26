// Translated from: backend/app/schemas/report.py (StatusCount)
package com.example.bwms.dto.report;

import com.example.bwms.model.TaskStatus;

public record StatusCountDto(TaskStatus status, int count) {}
