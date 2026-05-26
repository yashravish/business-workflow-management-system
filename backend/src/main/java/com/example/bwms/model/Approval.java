// Translated from: backend/app/models/approval.py
package com.example.bwms.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "approvals", indexes = {
        @Index(name = "ix_approvals_task_id",             columnList = "task_id"),
        @Index(name = "ix_approvals_approved_by_user_id", columnList = "approved_by_user_id"),
        @Index(name = "ix_approvals_decision",            columnList = "decision")
})
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "approved_by_user_id", nullable = false)
    private Long approvedByUserId;

    @Column(nullable = false)
    private ApprovalDecision decision;

    @Column(length = 500)
    private String comment;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id", insertable = false, updatable = false)
    private User approvedBy;

    public Approval() {}

    public Approval(Long taskId, Long approvedByUserId, ApprovalDecision decision, String comment) {
        this.taskId = taskId;
        this.approvedByUserId = approvedByUserId;
        this.decision = decision;
        this.comment = comment;
    }

    public Long getId()                   { return id; }
    public Long getTaskId()               { return taskId; }
    public Long getApprovedByUserId()     { return approvedByUserId; }
    public ApprovalDecision getDecision() { return decision; }
    public String getComment()            { return comment; }
    public OffsetDateTime getCreatedAt()  { return createdAt; }
    public Task getTask()                 { return task; }
    public User getApprovedBy()           { return approvedBy; }
}
