// Translated from: backend/app/schemas/report.py (ApprovalSummaryEntry)
package com.example.bwms.dto.report;

import com.example.bwms.model.ApprovalDecision;

public record ApprovalSummaryEntry(ApprovalDecision decision, int count) {}
