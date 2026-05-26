// Translated from: backend/app/models/audit_log.py
package com.example.bwms.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum AuditAction {
    LOGIN("login"),
    CREATE_TASK("create_task"),
    UPDATE_TASK("update_task"),
    DELETE_TASK("delete_task"),
    SUBMIT_TASK("submit_task"),
    APPROVE_TASK("approve_task"),
    REJECT_TASK("reject_task"),
    COMPLETE_TASK("complete_task");

    private final String value;

    AuditAction(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AuditAction fromValue(String value) {
        for (AuditAction a : values()) {
            if (a.value.equals(value)) return a;
        }
        throw new IllegalArgumentException("Unknown audit action: " + value);
    }

    @Converter(autoApply = true)
    public static class JpaConverter implements AttributeConverter<AuditAction, String> {
        @Override
        public String convertToDatabaseColumn(AuditAction attr) {
            return attr == null ? null : attr.getValue();
        }

        @Override
        public AuditAction convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fromValue(dbData);
        }
    }
}
