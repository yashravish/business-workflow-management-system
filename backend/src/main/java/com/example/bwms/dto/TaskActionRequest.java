// Translated from: backend/app/schemas/task.py (TaskActionRequest)
package com.example.bwms.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

public record TaskActionRequest(@Nullable @Size(max = 500) String comment) {}
