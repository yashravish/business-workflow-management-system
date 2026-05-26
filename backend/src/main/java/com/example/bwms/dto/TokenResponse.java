// Translated from: backend/app/schemas/auth.py (TokenResponse)
package com.example.bwms.dto;

public record TokenResponse(String accessToken, String tokenType, UserReadDto user) {

    public static TokenResponse bearer(String token, UserReadDto user) {
        return new TokenResponse(token, "bearer", user);
    }
}
