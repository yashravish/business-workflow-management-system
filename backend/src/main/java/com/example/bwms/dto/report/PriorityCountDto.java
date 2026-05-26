// Translated from: backend/app/schemas/report.py (PriorityCount)
package com.example.bwms.dto.report;

import com.example.bwms.model.TaskPriority;

public record PriorityCountDto(TaskPriority priority, int count) {}
