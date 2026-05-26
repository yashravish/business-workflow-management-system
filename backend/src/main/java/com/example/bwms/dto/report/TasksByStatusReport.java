// Translated from: backend/app/schemas/report.py (TasksByStatusReport)
package com.example.bwms.dto.report;

import java.util.List;

public record TasksByStatusReport(List<StatusCountDto> items, int total) {}
