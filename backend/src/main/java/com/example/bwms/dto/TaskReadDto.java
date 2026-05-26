// Translated from: backend/app/schemas/task.py (TaskRead)
package com.example.bwms.dto;

import com.example.bwms.model.Task;
import com.example.bwms.model.TaskPriority;
import com.example.bwms.model.TaskStatus;

import java.time.OffsetDateTime;

public record TaskReadDto(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Long assignedToUserId,
        Long createdByUserId,
        UserSummaryDto assignee,
        UserSummaryDto creator,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static TaskReadDto from(Task task) {
        return new TaskReadDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getAssignedToUserId(),
                task.getCreatedByUserId(),
                UserSummaryDto.from(task.getAssignee()),
                UserSummaryDto.from(task.getCreator()),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
