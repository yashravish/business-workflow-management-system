// Translated from: backend/app/api/routes/tasks.py
package com.example.bwms.controller;

import com.example.bwms.core.UserPrincipal;
import com.example.bwms.dto.TaskActionRequest;
import com.example.bwms.dto.TaskCreateDto;
import com.example.bwms.dto.TaskReadDto;
import com.example.bwms.dto.TaskUpdateDto;
import com.example.bwms.dto.WorkflowEventReadDto;
import com.example.bwms.model.TaskPriority;
import com.example.bwms.model.TaskStatus;
import com.example.bwms.model.User;
import com.example.bwms.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Python: GET /tasks
    @GetMapping
    public List<TaskReadDto> listTasks(
            @RequestParam @Nullable TaskStatus status,
            @RequestParam @Nullable TaskPriority priority,
            @RequestParam @Nullable Long assignedToUserId) {
        return taskService.listTasks(status, priority, assignedToUserId).stream()
                .map(TaskReadDto::from)
                .toList();
    }

    // Python: POST /tasks — 201 Created
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskReadDto createTask(@Valid @RequestBody TaskCreateDto payload,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        return TaskReadDto.from(taskService.createTask(payload, principal.getUser()));
    }

    // Python: GET /tasks/{task_id}
    @GetMapping("/{taskId}")
    public TaskReadDto getTask(@PathVariable Long taskId) {
        return TaskReadDto.from(taskService.getTaskOr404(taskId));
    }

    // Python: PUT /tasks/{task_id}
    @PutMapping("/{taskId}")
    public TaskReadDto updateTask(@PathVariable Long taskId,
                                  @Valid @RequestBody TaskUpdateDto payload,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        return TaskReadDto.from(taskService.updateTask(taskId, payload, principal.getUser()));
    }

    // Python: DELETE /tasks/{task_id} — 204 No Content
    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long taskId,
                           @AuthenticationPrincipal UserPrincipal principal) {
        taskService.deleteTask(taskId, principal.getUser());
    }

    // Python: POST /tasks/{task_id}/submit
    @PostMapping("/{taskId}/submit")
    public TaskReadDto submitTask(@PathVariable Long taskId,
                                  @RequestBody(required = false) TaskActionRequest payload,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        String comment = payload != null ? payload.comment() : null;
        return TaskReadDto.from(taskService.submitTask(taskId, principal.getUser(), comment));
    }

    // Python: POST /tasks/{task_id}/approve — manager only
    @PostMapping("/{taskId}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public TaskReadDto approveTask(@PathVariable Long taskId,
                                   @RequestBody(required = false) TaskActionRequest payload,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        User manager = principal.getUser();
        String comment = payload != null ? payload.comment() : null;
        return TaskReadDto.from(taskService.approveTask(taskId, manager, comment));
    }

    // Python: POST /tasks/{task_id}/reject — manager only
    @PostMapping("/{taskId}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public TaskReadDto rejectTask(@PathVariable Long taskId,
                                  @RequestBody(required = false) TaskActionRequest payload,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        User manager = principal.getUser();
        String comment = payload != null ? payload.comment() : null;
        return TaskReadDto.from(taskService.rejectTask(taskId, manager, comment));
    }

    // Python: POST /tasks/{task_id}/complete — manager only
    @PostMapping("/{taskId}/complete")
    @PreAuthorize("hasRole('MANAGER')")
    public TaskReadDto completeTask(@PathVariable Long taskId,
                                    @RequestBody(required = false) TaskActionRequest payload,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        User manager = principal.getUser();
        String comment = payload != null ? payload.comment() : null;
        return TaskReadDto.from(taskService.completeTask(taskId, manager, comment));
    }

    // Python: GET /tasks/{task_id}/workflow-events
    @GetMapping("/{taskId}/workflow-events")
    public List<WorkflowEventReadDto> listWorkflowEvents(@PathVariable Long taskId) {
        taskService.getTaskOr404(taskId);
        return taskService.getTaskHistory(taskId).stream()
                .map(WorkflowEventReadDto::from)
                .toList();
    }
}
