// Translated from: backend/app/api/routes/reports.py
package com.example.bwms.controller;

import com.example.bwms.dto.report.*;
import com.example.bwms.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // Python: GET /reports/tasks-by-status
    @GetMapping("/tasks-by-status")
    public TasksByStatusReport tasksByStatus() {
        return reportService.tasksByStatus();
    }

    // Python: GET /reports/tasks-by-priority
    @GetMapping("/tasks-by-priority")
    public TasksByPriorityReport tasksByPriority() {
        return reportService.tasksByPriority();
    }

    // Python: GET /reports/user-workload
    @GetMapping("/user-workload")
    public UserWorkloadReport userWorkload() {
        return reportService.userWorkload();
    }

    // Python: GET /reports/approval-summary
    @GetMapping("/approval-summary")
    public ApprovalSummaryReport approvalSummary() {
        return reportService.approvalSummary();
    }

    // Python: GET /reports/export/tasks.csv — returns text/csv with Content-Disposition header
    @GetMapping("/export/tasks.csv")
    public ResponseEntity<String> exportTasksCsv() {
        String csv = reportService.exportTasksCsv();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tasks.csv")
                .body(csv);
    }
}
