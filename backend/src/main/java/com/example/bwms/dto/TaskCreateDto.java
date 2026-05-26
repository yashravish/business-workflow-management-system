// Translated from: backend/app/schemas/task.py (TaskCreate)
package com.example.bwms.dto;

import com.example.bwms.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskCreateDto(
        @NotBlank @Size(min = 1, max = 200) String title,
        @Size(max = 5000) String description,
        TaskPriority priority,
        @NotNull Long assignedToUserId
) {
    public TaskCreateDto {
        if (description == null) description = "";
        if (priority == null) priority = TaskPriority.MEDIUM;
    }
}
