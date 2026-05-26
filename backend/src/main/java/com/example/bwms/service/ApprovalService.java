// Translated from: backend/app/services/approval_service.py
package com.example.bwms.service;

import com.example.bwms.model.*;
import com.example.bwms.repository.ApprovalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final WorkflowService workflowService;

    public ApprovalService(ApprovalRepository approvalRepository, WorkflowService workflowService) {
        this.approvalRepository = approvalRepository;
        this.workflowService = workflowService;
    }

    private void ensureManager(User user) {
        if (!user.isManager()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only managers can approve or reject tasks.");
        }
    }

    private void ensureSubmitted(Task task) {
        if (task.getStatus() != TaskStatus.SUBMITTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Only submitted tasks can be approved or rejected.");
        }
    }

    // Python: ApprovalService.approve_task
    // @Transactional boundary owned by calling TaskService method
    public Approval approveTask(Task task, User manager, @Nullable String comment) {
        return recordDecision(task, manager, comment,
                ApprovalDecision.APPROVED, TaskStatus.APPROVED, "Approved by manager");
    }

    // Python: ApprovalService.reject_task
    public Approval rejectTask(Task task, User manager, @Nullable String comment) {
        return recordDecision(task, manager, comment,
                ApprovalDecision.REJECTED, TaskStatus.REJECTED, "Rejected by manager");
    }

    private Approval recordDecision(Task task, User manager, @Nullable String comment,
                                    ApprovalDecision decision, TaskStatus targetStatus,
                                    String defaultNote) {
        ensureManager(manager);
        ensureSubmitted(task);
        workflowService.transition(task, targetStatus, manager,
                comment != null ? comment : defaultNote);
        Approval approval = new Approval(
                task.getId(), manager.getId(), decision, comment);
        return approvalRepository.save(approval);
    }
}
