// Translated from: backend/app/models/audit_log.py
package com.example.bwms.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "ix_audit_logs_user_id",     columnList = "user_id"),
        @Index(name = "ix_audit_logs_action",      columnList = "action"),
        @Index(name = "ix_audit_logs_entity_type", columnList = "entity_type"),
        @Index(name = "ix_audit_logs_entity_id",   columnList = "entity_id"),
        @Index(name = "ix_audit_logs_created_at",  columnList = "created_at")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nullable: system actions may not be tied to a user
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private AuditAction action;

    @Column(name = "entity_type", length = 50, nullable = false)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    public AuditLog() {}

    public AuditLog(Long userId, AuditAction action, String entityType,
                    Long entityId, String details) {
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
    }

    public Long getId()                  { return id; }
    public Long getUserId()              { return userId; }
    public AuditAction getAction()       { return action; }
    public String getEntityType()        { return entityType; }
    public Long getEntityId()            { return entityId; }
    public String getDetails()           { return details; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public User getUser()                { return user; }
}
