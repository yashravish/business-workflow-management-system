// Translated from: backend/app/models/workflow_event.py
package com.example.bwms.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "workflow_events", indexes = {
        @Index(name = "ix_workflow_events_task_id",            columnList = "task_id"),
        @Index(name = "ix_workflow_events_changed_by_user_id", columnList = "changed_by_user_id")
})
public class WorkflowEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    // Nullable: first event (task creation) has no prior status
    @Column(name = "from_status")
    private TaskStatus fromStatus;

    @Column(name = "to_status", nullable = false)
    private TaskStatus toStatus;

    @Column(name = "changed_by_user_id", nullable = false)
    private Long changedByUserId;

    @Column(length = 500)
    private String note;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id", insertable = false, updatable = false)
    private User changedBy;

    public WorkflowEvent() {}

    public WorkflowEvent(Long taskId, TaskStatus fromStatus, TaskStatus toStatus,
                         Long changedByUserId, String note) {
        this.taskId = taskId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedByUserId = changedByUserId;
        this.note = note;
    }

    public Long getId()                  { return id; }
    public Long getTaskId()              { return taskId; }
    public TaskStatus getFromStatus()    { return fromStatus; }
    public TaskStatus getToStatus()      { return toStatus; }
    public Long getChangedByUserId()     { return changedByUserId; }
    public String getNote()              { return note; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public Task getTask()                { return task; }
    public User getChangedBy()           { return changedBy; }
}
