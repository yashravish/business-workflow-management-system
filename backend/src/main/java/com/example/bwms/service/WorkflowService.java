// Translated from: backend/app/services/workflow_service.py
package com.example.bwms.service;

import com.example.bwms.model.*;
import com.example.bwms.repository.WorkflowEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Service
public class WorkflowService {

    // Python: VALID_TRANSITIONS dict — single source of truth for allowed state changes
    public static final Map<TaskStatus, EnumSet<TaskStatus>> VALID_TRANSITIONS;
    static {
        VALID_TRANSITIONS = new EnumMap<>(TaskStatus.class);
        VALID_TRANSITIONS.put(TaskStatus.PENDING,    EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.SUBMITTED));
        VALID_TRANSITIONS.put(TaskStatus.IN_PROGRESS, EnumSet.of(TaskStatus.SUBMITTED));
        VALID_TRANSITIONS.put(TaskStatus.SUBMITTED,  EnumSet.of(TaskStatus.APPROVED, TaskStatus.REJECTED));
        VALID_TRANSITIONS.put(TaskStatus.APPROVED,   EnumSet.of(TaskStatus.COMPLETED));
        VALID_TRANSITIONS.put(TaskStatus.REJECTED,   EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.SUBMITTED));
        VALID_TRANSITIONS.put(TaskStatus.COMPLETED,  EnumSet.noneOf(TaskStatus.class));
    }

    private final WorkflowEventRepository workflowEventRepository;

    public WorkflowService(WorkflowEventRepository workflowEventRepository) {
        this.workflowEventRepository = workflowEventRepository;
    }

    // Python: WorkflowService.is_valid_transition (static)
    public static boolean isValidTransition(TaskStatus from, TaskStatus to) {
        EnumSet<TaskStatus> allowed = VALID_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    // Python: WorkflowService.assert_valid_transition → HTTP 400
    public static void assertValidTransition(TaskStatus from, TaskStatus to) {
        if (!isValidTransition(from, to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status transition: " + from.getValue() + " -> " + to.getValue()
            );
        }
    }

    // Python: WorkflowService.transition — mutates task, emits WorkflowEvent
    // @Transactional boundary owned by the calling service method
    public WorkflowEvent transition(Task task, TaskStatus toStatus,
                                    User changedBy, @Nullable String note) {
        TaskStatus fromStatus = task.getStatus();
        assertValidTransition(fromStatus, toStatus);
        task.setStatus(toStatus);
        WorkflowEvent event = new WorkflowEvent(
                task.getId(), fromStatus, toStatus, changedBy.getId(), note);
        return workflowEventRepository.save(event);
    }

    // Python: WorkflowService.record_initial_event — fromStatus is null for task creation
    public WorkflowEvent recordInitialEvent(Task task, User changedBy, @Nullable String note) {
        WorkflowEvent event = new WorkflowEvent(
                task.getId(), null, task.getStatus(),
                changedBy.getId(), note != null ? note : "Task created");
        return workflowEventRepository.save(event);
    }

    public List<WorkflowEvent> getTaskHistory(Long taskId) {
        return workflowEventRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
    }
}
