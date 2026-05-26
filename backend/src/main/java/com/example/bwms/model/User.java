// Translated from: backend/app/models/user.py
package com.example.bwms.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "ix_users_email", columnList = "email", unique = true),
        @Index(name = "ix_users_role",  columnList = "role")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120, nullable = false)
    private String name;

    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @Column(length = 255, nullable = false)
    private String hashedPassword;

    @Column(nullable = false)
    private UserRole role;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private List<Task> createdTasks = new ArrayList<>();

    @OneToMany(mappedBy = "assignee", fetch = FetchType.LAZY)
    private List<Task> assignedTasks = new ArrayList<>();

    @OneToMany(mappedBy = "changedBy", fetch = FetchType.LAZY)
    private List<WorkflowEvent> workflowEvents = new ArrayList<>();

    @OneToMany(mappedBy = "approvedBy", fetch = FetchType.LAZY)
    private List<Approval> approvals = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<AuditLog> auditLogs = new ArrayList<>();

    public User() {}

    public User(String name, String email, String hashedPassword, UserRole role) {
        this.name = name;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.role = role;
    }

    public boolean isManager() { return role == UserRole.MANAGER; }
    public boolean isAnalyst()  { return role == UserRole.ANALYST; }

    public Long getId()              { return id; }
    public String getName()          { return name; }
    public String getEmail()         { return email; }
    public String getHashedPassword(){ return hashedPassword; }
    public UserRole getRole()        { return role; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void setName(String name)          { this.name = name; }
    public void setEmail(String email)        { this.email = email; }
    public void setHashedPassword(String hp)  { this.hashedPassword = hp; }
    public void setRole(UserRole role)        { this.role = role; }
}
