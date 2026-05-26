// Translated from: backend/app/services/task_service.py
package com.example.bwms.service;

import com.example.bwms.dto.TaskCreateDto;
import com.example.bwms.dto.TaskUpdateDto;
import com.example.bwms.model.*;
import com.example.bwms.repository.TaskRepository;
import com.example.bwms.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WorkflowService workflowService;
    private final ApprovalService approvalService;
    private final AuditLogService auditLogService;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository,
                       WorkflowService workflowService, ApprovalService approvalService,
                       AuditLogService auditLogService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.workflowService = workflowService;
        this.approvalService = approvalService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<Task> listTasks(@Nullable TaskStatus status,
                                @Nullable TaskPriority priority,
                                @Nullable Long assignedToUserId) {
        Specification<Task> spec = (root, query, cb) -> {
            // Skip eager fetches on the count query Spring may issue
            if (query.getResultType() == Task.class) {
                root.fetch("assignee", JoinType.LEFT);
                root.fetch("creator", JoinType.LEFT);
            }
            List<Predicate> predicates = new ArrayList<>();
            if (status != null)             predicates.add(cb.equal(root.get("status"), status));
            if (priority != null)           predicates.add(cb.equal(root.get("priority"), priority));
            if (assignedToUserId != null)   predicates.add(cb.equal(root.get("assignedToUserId"), assignedToUserId));
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
        return taskRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional(readOnly = true)
    public Task getTaskOr404(Long taskId) {
        return taskRepository.findById(taskId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Task " + taskId + " not found."));
    }

    // Python: TaskService._ensure_creator_or_manager
    private void ensureCreatorOrManager(Task task, User user, String action) {
        if (user.isAnalyst() && !task.getCreatedByUserId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Analysts can only " + action + " tasks they created.");
        }
    }

    @Transactional
    public Task createTask(TaskCreateDto data, User currentUser) {
        User assignee = userRepository.findById(data.assignedToUserId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Assignee user " + data.assignedToUserId() + " not found."));

        Task task = new Task();
        task.setTitle(data.title().strip());
        task.setDescription(data.description() != null ? data.description() : "");
        task.setPriority(data.priority() != null ? data.priority() : TaskPriority.MEDIUM);
        task.setStatus(TaskStatus.PENDING);
        task.setAssignedToUserId(assignee.getId());
        task.setCreatedByUserId(currentUser.getId());
        taskRepository.save(task);

        workflowService.recordInitialEvent(task, currentUser, null);
        auditLogService.logAction(currentUser.getId(), AuditAction.CREATE_TASK,
                "task", task.getId(),
                "Created task '" + task.getTitle() + "' assigned to user " + assignee.getId());

        // Detach from first-level cache so the reload below re-queries with the EntityGraph
        // (without detach, Hibernate returns the cached entity which has null lazy associations)
        entityManager.detach(task);
        return taskRepository.findById(task.getId()).orElseThrow();
    }

    @Transactional
    public Task updateTask(Long taskId, TaskUpdateDto data, User currentUser) {
        Task task = getTaskOr404(taskId);
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Completed tasks cannot be edited.");
        }
        ensureCreatorOrManager(task, currentUser, "edit");

        List<String> changedFields = new ArrayList<>();
        if (data.title() != null && !data.title().strip().equals(task.getTitle())) {
            task.setTitle(data.title().strip());
            changedFields.add("title");
        }
        if (data.description() != null && !data.description().equals(task.getDescription())) {
            task.setDescription(data.description());
            changedFields.add("description");
        }
        if (data.priority() != null && data.priority() != task.getPriority()) {
            task.setPriority(data.priority());
            changedFields.add("priority");
        }
        if (data.assignedToUserId() != null
                && !data.assignedToUserId().equals(task.getAssignedToUserId())) {
            userRepository.findById(data.assignedToUserId()).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Assignee user " + data.assignedToUserId() + " not found."));
            task.setAssignedToUserId(data.assignedToUserId());
            changedFields.add("assigned_to_user_id");
        }

        String details = changedFields.isEmpty()
                ? "No-op update"
                : "Updated fields: " + String.join(", ", changedFields);
        auditLogService.logAction(currentUser.getId(), AuditAction.UPDATE_TASK,
                "task", task.getId(), details);

        taskRepository.save(task);
        return taskRepository.findById(task.getId()).orElseThrow();
    }

    @Transactional
    public void deleteTask(Long taskId, User currentUser) {
        Task task = getTaskOr404(taskId);
        if (task.getStatus() == TaskStatus.APPROVED || task.getStatus() == TaskStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Approved or completed tasks cannot be deleted.");
        }
        ensureCreatorOrManager(task, currentUser, "delete");

        Long taskIdValue = task.getId();
        String title = task.getTitle();
        taskRepository.delete(task);
        auditLogService.logAction(currentUser.getId(), AuditAction.DELETE_TASK,
                "task", taskIdValue, "Deleted task '" + title + "'");
    }

    @Transactional
    public Task submitTask(Long taskId, User currentUser, @Nullable String comment) {
        Task task = getTaskOr404(taskId);
        ensureCreatorOrManager(task, currentUser, "submit");
        workflowService.transition(task, TaskStatus.SUBMITTED, currentUser,
                comment != null ? comment : "Submitted for review");
        auditLogService.logAction(currentUser.getId(), AuditAction.SUBMIT_TASK,
                "task", task.getId(), comment != null ? comment : "Submitted task");
        taskRepository.save(task);
        return taskRepository.findById(task.getId()).orElseThrow();
    }

    @Transactional
    public Task approveTask(Long taskId, User manager, @Nullable String comment) {
        Task task = getTaskOr404(taskId);
        approvalService.approveTask(task, manager, comment);
        auditLogService.logAction(manager.getId(), AuditAction.APPROVE_TASK,
                "task", task.getId(), comment != null ? comment : "Approved task");
        taskRepository.save(task);
        return taskRepository.findById(task.getId()).orElseThrow();
    }

    @Transactional
    public Task rejectTask(Long taskId, User manager, @Nullable String comment) {
        Task task = getTaskOr404(taskId);
        approvalService.rejectTask(task, manager, comment);
        auditLogService.logAction(manager.getId(), AuditAction.REJECT_TASK,
                "task", task.getId(), comment != null ? comment : "Rejected task");
        taskRepository.save(task);
        return taskRepository.findById(task.getId()).orElseThrow();
    }

    @Transactional
    public Task completeTask(Long taskId, User manager, @Nullable String comment) {
        Task task = getTaskOr404(taskId);
        if (!manager.isManager()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only managers can complete tasks.");
        }
        workflowService.transition(task, TaskStatus.COMPLETED, manager,
                comment != null ? comment : "Completed by manager");
        auditLogService.logAction(manager.getId(), AuditAction.COMPLETE_TASK,
                "task", task.getId(), comment != null ? comment : "Completed task");
        taskRepository.save(task);
        return taskRepository.findById(task.getId()).orElseThrow();
    }

    public List<WorkflowEvent> getTaskHistory(Long taskId) {
        return workflowService.getTaskHistory(taskId);
    }
}
