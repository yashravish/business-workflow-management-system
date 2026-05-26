// Translated from: backend/app/schemas/workflow_event.py (WorkflowEventRead)
package com.example.bwms.dto;

import com.example.bwms.model.TaskStatus;
import com.example.bwms.model.WorkflowEvent;

import java.time.OffsetDateTime;

public record WorkflowEventReadDto(
        Long id,
        Long taskId,
        TaskStatus fromStatus,
        TaskStatus toStatus,
        Long changedByUserId,
        UserSummaryDto changedBy,
        String note,
        OffsetDateTime createdAt
) {
    public static WorkflowEventReadDto from(WorkflowEvent event) {
        return new WorkflowEventReadDto(
                event.getId(),
                event.getTaskId(),
                event.getFromStatus(),
                event.getToStatus(),
                event.getChangedByUserId(),
                UserSummaryDto.from(event.getChangedBy()),
                event.getNote(),
                event.getCreatedAt()
        );
    }
}
