// Translated from: backend/app/db/seed.py
// Idempotent: skips if manager@example.com already exists.
// Active on all profiles except "test" to avoid polluting integration test databases.
package com.example.bwms.db;

import com.example.bwms.model.*;
import com.example.bwms.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("!test")
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final WorkflowEventRepository workflowEventRepository;
    private final ApprovalRepository approvalRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, TaskRepository taskRepository,
                      WorkflowEventRepository workflowEventRepository,
                      ApprovalRepository approvalRepository,
                      AuditLogRepository auditLogRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.workflowEventRepository = workflowEventRepository;
        this.approvalRepository = approvalRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.findByEmail("manager@example.com").isPresent()) {
            log.info("Seed data already present, skipping.");
            return;
        }

        log.info("Seeding users...");
        String pw = passwordEncoder.encode("password123");
        User megan  = saveUser("Megan Manager",  "manager@example.com",        pw, UserRole.MANAGER);
        User marcus = saveUser("Marcus Director", "marcus.manager@example.com", pw, UserRole.MANAGER);
        User anna   = saveUser("Anna Analyst",    "analyst@example.com",        pw, UserRole.ANALYST);
        User brian  = saveUser("Brian Carter",    "brian.analyst@example.com",  pw, UserRole.ANALYST);
        User carla  = saveUser("Carla Reyes",     "carla.analyst@example.com",  pw, UserRole.ANALYST);

        Map<String, User> byEmail = new HashMap<>();
        byEmail.put(megan.getEmail(), megan);   byEmail.put(marcus.getEmail(), marcus);
        byEmail.put(anna.getEmail(), anna);     byEmail.put(brian.getEmail(), brian);
        byEmail.put(carla.getEmail(), carla);

        log.info("Seeding tasks, workflow events, approvals, and audit logs...");
        User primaryManager = megan;

        record SeedTask(String title, String description, TaskPriority priority,
                        TaskStatus status, String assigneeEmail, String creatorEmail) {}

        List<SeedTask> specs = List.of(
            new SeedTask("Q3 vendor contract renewal review",
                "Review renewal terms for vendor #482, flag pricing changes.",
                TaskPriority.HIGH, TaskStatus.PENDING, "analyst@example.com", "analyst@example.com"),
            new SeedTask("Reconcile June expense report variances",
                "Compare GL postings to expense reports and document gaps.",
                TaskPriority.MEDIUM, TaskStatus.IN_PROGRESS, "analyst@example.com", "analyst@example.com"),
            new SeedTask("Customer onboarding SLA audit",
                "Audit last 30 onboarding tickets for SLA breaches.",
                TaskPriority.MEDIUM, TaskStatus.SUBMITTED, "brian.analyst@example.com", "brian.analyst@example.com"),
            new SeedTask("Update procurement workflow documentation",
                "Refresh runbook with the new approval thresholds.",
                TaskPriority.LOW, TaskStatus.SUBMITTED, "carla.analyst@example.com", "carla.analyst@example.com"),
            new SeedTask("Investigate duplicate invoice posting INV-2034",
                "Trace duplicate posting and submit corrective entry.",
                TaskPriority.HIGH, TaskStatus.APPROVED, "analyst@example.com", "analyst@example.com"),
            new SeedTask("Annual access review - Finance group",
                "Confirm role mappings for the finance Active Directory group.",
                TaskPriority.MEDIUM, TaskStatus.COMPLETED, "brian.analyst@example.com", "brian.analyst@example.com"),
            new SeedTask("Dashboard data refresh failure RCA",
                "Root-cause the Tuesday morning ETL failure and document remediation.",
                TaskPriority.HIGH, TaskStatus.REJECTED, "carla.analyst@example.com", "carla.analyst@example.com"),
            new SeedTask("Quarterly headcount reconciliation",
                "Reconcile HRIS headcount against the GL allocation file.",
                TaskPriority.MEDIUM, TaskStatus.PENDING, "carla.analyst@example.com", "carla.analyst@example.com"),
            new SeedTask("Refactor month-end close checklist",
                "Modernize the close checklist template based on Q2 retro feedback.",
                TaskPriority.LOW, TaskStatus.IN_PROGRESS, "brian.analyst@example.com", "brian.analyst@example.com"),
            new SeedTask("Approve travel reimbursement batch #214",
                "Validate batch totals and route to AP for payment.",
                TaskPriority.MEDIUM, TaskStatus.SUBMITTED, "analyst@example.com", "analyst@example.com"),
            new SeedTask("Vendor risk assessment - data processor X",
                "Complete the risk assessment template and attach SOC 2.",
                TaskPriority.HIGH, TaskStatus.APPROVED, "carla.analyst@example.com", "carla.analyst@example.com"),
            new SeedTask("Backfill missing audit log entries for May 14",
                "Coordinate with platform team to replay missing audit events.",
                TaskPriority.LOW, TaskStatus.COMPLETED, "analyst@example.com", "analyst@example.com")
        );

        List<Task> createdTasks = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();
        for (SeedTask spec : specs) {
            User assignee = byEmail.get(spec.assigneeEmail());
            User creator  = byEmail.get(spec.creatorEmail());

            Task task = new Task();
            task.setTitle(spec.title());
            task.setDescription(spec.description());
            task.setPriority(spec.priority());
            task.setStatus(TaskStatus.PENDING);
            task.setAssignedToUserId(assignee.getId());
            task.setCreatedByUserId(creator.getId());
            task.setCreatedAt(now);
            task.setUpdatedAt(now);
            taskRepository.save(task);

            driveStatus(task, spec.status(), creator, primaryManager);
            createdTasks.add(task);
        }

        addAuditLogs(primaryManager, anna, createdTasks);
        log.info("Seed complete: 5 users, {} tasks.", createdTasks.size());
    }

    // Python: _drive_status — walks the state machine to reach targetStatus
    private void driveStatus(Task task, TaskStatus targetStatus, User creator, User manager) {
        record Event(TaskStatus from, TaskStatus to, User actor, String note) {}

        List<Event> history = new ArrayList<>();
        history.add(new Event(null, TaskStatus.PENDING, creator, "Task created"));

        if (targetStatus == TaskStatus.PENDING) {
            // no additional events
        } else if (targetStatus == TaskStatus.IN_PROGRESS) {
            history.add(new Event(TaskStatus.PENDING, TaskStatus.IN_PROGRESS, creator, "Started work"));
        } else if (targetStatus == TaskStatus.SUBMITTED) {
            history.add(new Event(TaskStatus.PENDING, TaskStatus.IN_PROGRESS, creator, "Started work"));
            history.add(new Event(TaskStatus.IN_PROGRESS, TaskStatus.SUBMITTED, creator, "Submitted for review"));
        } else if (targetStatus == TaskStatus.APPROVED) {
            history.add(new Event(TaskStatus.PENDING, TaskStatus.IN_PROGRESS, creator, "Started work"));
            history.add(new Event(TaskStatus.IN_PROGRESS, TaskStatus.SUBMITTED, creator, "Submitted for review"));
            history.add(new Event(TaskStatus.SUBMITTED, TaskStatus.APPROVED, manager, "Approved by manager"));
        } else if (targetStatus == TaskStatus.REJECTED) {
            history.add(new Event(TaskStatus.PENDING, TaskStatus.IN_PROGRESS, creator, "Started work"));
            history.add(new Event(TaskStatus.IN_PROGRESS, TaskStatus.SUBMITTED, creator, "Submitted for review"));
            history.add(new Event(TaskStatus.SUBMITTED, TaskStatus.REJECTED, manager, "Needs more detail before approval"));
        } else if (targetStatus == TaskStatus.COMPLETED) {
            history.add(new Event(TaskStatus.PENDING, TaskStatus.IN_PROGRESS, creator, "Started work"));
            history.add(new Event(TaskStatus.IN_PROGRESS, TaskStatus.SUBMITTED, creator, "Submitted for review"));
            history.add(new Event(TaskStatus.SUBMITTED, TaskStatus.APPROVED, manager, "Approved by manager"));
            history.add(new Event(TaskStatus.APPROVED, TaskStatus.COMPLETED, manager, "Completed"));
        }

        for (Event e : history) {
            workflowEventRepository.save(new WorkflowEvent(task.getId(), e.from(), e.to(), e.actor().getId(), e.note()));
        }

        task.setStatus(targetStatus);
        taskRepository.save(task);

        if (targetStatus == TaskStatus.APPROVED) {
            approvalRepository.save(new Approval(task.getId(), manager.getId(),
                    ApprovalDecision.APPROVED, "Looks good - proceed."));
        } else if (targetStatus == TaskStatus.REJECTED) {
            approvalRepository.save(new Approval(task.getId(), manager.getId(),
                    ApprovalDecision.REJECTED, "Please add supporting evidence."));
        } else if (targetStatus == TaskStatus.COMPLETED) {
            approvalRepository.save(new Approval(task.getId(), manager.getId(),
                    ApprovalDecision.APPROVED, "Approved and later completed."));
        }
    }

    // Python: _add_audit_logs
    private void addAuditLogs(User manager, User analyst, List<Task> tasks) {
        auditLogRepository.save(new AuditLog(manager.getId(), AuditAction.LOGIN,
                "user", manager.getId(), "User " + manager.getEmail() + " logged in"));
        auditLogRepository.save(new AuditLog(analyst.getId(), AuditAction.LOGIN,
                "user", analyst.getId(), "User " + analyst.getEmail() + " logged in"));

        for (Task task : tasks) {
            auditLogRepository.save(new AuditLog(task.getCreatedByUserId(), AuditAction.CREATE_TASK,
                    "task", task.getId(), "Created task '" + task.getTitle() + "'"));

            if (task.getStatus() == TaskStatus.SUBMITTED || task.getStatus() == TaskStatus.APPROVED
                    || task.getStatus() == TaskStatus.REJECTED || task.getStatus() == TaskStatus.COMPLETED) {
                auditLogRepository.save(new AuditLog(task.getCreatedByUserId(), AuditAction.SUBMIT_TASK,
                        "task", task.getId(), "Submitted task"));
            }
            if (task.getStatus() == TaskStatus.APPROVED) {
                auditLogRepository.save(new AuditLog(manager.getId(), AuditAction.APPROVE_TASK,
                        "task", task.getId(), "Approved task"));
            } else if (task.getStatus() == TaskStatus.REJECTED) {
                auditLogRepository.save(new AuditLog(manager.getId(), AuditAction.REJECT_TASK,
                        "task", task.getId(), "Rejected task"));
            } else if (task.getStatus() == TaskStatus.COMPLETED) {
                auditLogRepository.save(new AuditLog(manager.getId(), AuditAction.APPROVE_TASK,
                        "task", task.getId(), "Approved task"));
                auditLogRepository.save(new AuditLog(manager.getId(), AuditAction.COMPLETE_TASK,
                        "task", task.getId(), "Completed task"));
            }
        }
    }

    private User saveUser(String name, String email, String hashedPw, UserRole role) {
        return userRepository.save(new User(name, email, hashedPw, role));
    }
}
