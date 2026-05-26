// Translated from: backend/app/api/routes/auth.py
package com.example.bwms.controller;

import com.example.bwms.core.UserPrincipal;
import com.example.bwms.dto.LoginRequest;
import com.example.bwms.dto.TokenResponse;
import com.example.bwms.dto.UserReadDto;
import com.example.bwms.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Python: POST /auth/login
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        AuthService.LoginResult result = authService.login(request.email(), request.password());
        return TokenResponse.bearer(result.token(), UserReadDto.from(result.user()));
    }

    // Python: GET /auth/me
    @GetMapping("/me")
    public UserReadDto me(@AuthenticationPrincipal UserPrincipal principal) {
        return UserReadDto.from(principal.getUser());
    }
}
