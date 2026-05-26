// Translated from: backend/app/schemas/task.py (TaskUpdate) — all fields optional for partial update
package com.example.bwms.dto;

import com.example.bwms.model.TaskPriority;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

public record TaskUpdateDto(
        @Nullable @Size(min = 1, max = 200) String title,
        @Nullable @Size(max = 5000) String description,
        @Nullable TaskPriority priority,
        @Nullable Long assignedToUserId
) {}
