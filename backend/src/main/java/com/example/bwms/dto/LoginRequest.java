// Translated from: backend/app/schemas/auth.py (LoginRequest)
package com.example.bwms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 1, max = 128) String password
) {}
