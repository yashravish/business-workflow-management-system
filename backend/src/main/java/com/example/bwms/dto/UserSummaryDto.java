// Translated from: backend/app/schemas/user.py (UserSummary)
package com.example.bwms.dto;

import com.example.bwms.model.User;
import com.example.bwms.model.UserRole;

public record UserSummaryDto(Long id, String name, String email, UserRole role) {

    public static UserSummaryDto from(User user) {
        return new UserSummaryDto(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
