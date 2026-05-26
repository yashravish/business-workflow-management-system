// Translated from: backend/app/schemas/report.py (ApprovalSummaryReport)
package com.example.bwms.dto.report;

import java.util.List;

public record ApprovalSummaryReport(List<ApprovalSummaryEntry> items, int total) {}
