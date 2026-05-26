// Translated from: backend/app/models/task.py
package com.example.bwms.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum TaskPriority {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    private final String value;

    TaskPriority(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TaskPriority fromValue(String value) {
        for (TaskPriority p : values()) {
            if (p.value.equals(value)) return p;
        }
        throw new IllegalArgumentException("Unknown priority: " + value);
    }

    @Converter(autoApply = true)
    public static class JpaConverter implements AttributeConverter<TaskPriority, String> {
        @Override
        public String convertToDatabaseColumn(TaskPriority attr) {
            return attr == null ? null : attr.getValue();
        }

        @Override
        public TaskPriority convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fromValue(dbData);
        }
    }
}
