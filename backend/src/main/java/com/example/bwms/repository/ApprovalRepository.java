// Translated from: backend/app/models/approval.py (relationship mapping)
package com.example.bwms.repository;

import com.example.bwms.model.Approval;
import com.example.bwms.model.ApprovalDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    // Used by ReportService.approval_summary()
    @Query("SELECT a.decision, COUNT(a) FROM Approval a GROUP BY a.decision")
    List<Object[]> countByDecision();
}
