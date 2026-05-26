// Translated from: backend/app/models/approval.py
package com.example.bwms.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum ApprovalDecision {
    APPROVED("approved"),
    REJECTED("rejected");

    private final String value;

    ApprovalDecision(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ApprovalDecision fromValue(String value) {
        for (ApprovalDecision d : values()) {
            if (d.value.equals(value)) return d;
        }
        throw new IllegalArgumentException("Unknown decision: " + value);
    }

    @Converter(autoApply = true)
    public static class JpaConverter implements AttributeConverter<ApprovalDecision, String> {
        @Override
        public String convertToDatabaseColumn(ApprovalDecision attr) {
            return attr == null ? null : attr.getValue();
        }

        @Override
        public ApprovalDecision convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fromValue(dbData);
        }
    }
}
