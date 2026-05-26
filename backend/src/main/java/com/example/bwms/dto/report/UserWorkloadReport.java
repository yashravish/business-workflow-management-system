// Translated from: backend/app/schemas/report.py (UserWorkloadReport)
package com.example.bwms.dto.report;

import java.util.List;

public record UserWorkloadReport(List<UserWorkloadEntry> items) {}
