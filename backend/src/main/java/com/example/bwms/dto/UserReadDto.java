// Translated from: backend/app/schemas/user.py (UserRead)
package com.example.bwms.dto;

import com.example.bwms.model.User;
import com.example.bwms.model.UserRole;

import java.time.OffsetDateTime;

public record UserReadDto(
        Long id,
        String name,
        String email,
        UserRole role,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static UserReadDto from(User user) {
        return new UserReadDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
