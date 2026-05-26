// Translated from: backend/app/models/task.py
package com.example.bwms.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum TaskStatus {
    PENDING("pending"),
    IN_PROGRESS("in_progress"),
    SUBMITTED("submitted"),
    APPROVED("approved"),
    REJECTED("rejected"),
    COMPLETED("completed");

    private final String value;

    TaskStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TaskStatus fromValue(String value) {
        for (TaskStatus s : values()) {
            if (s.value.equals(value)) return s;
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }

    @Converter(autoApply = true)
    public static class JpaConverter implements AttributeConverter<TaskStatus, String> {
        @Override
        public String convertToDatabaseColumn(TaskStatus attr) {
            return attr == null ? null : attr.getValue();
        }

        @Override
        public TaskStatus convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fromValue(dbData);
        }
    }
}
