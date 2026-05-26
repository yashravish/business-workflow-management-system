// Translated from: backend/app/schemas/report.py (TasksByPriorityReport)
package com.example.bwms.dto.report;

import java.util.List;

public record TasksByPriorityReport(List<PriorityCountDto> items, int total) {}
