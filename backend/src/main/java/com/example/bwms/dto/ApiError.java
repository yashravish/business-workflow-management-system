// Matches FastAPI's {"detail": "..."} error response format
package com.example.bwms.dto;

public record ApiError(String detail) {}
