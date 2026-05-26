// Translated from: backend/app/services/auth_service.py
package com.example.bwms.service;

import com.example.bwms.core.JwtService;
import com.example.bwms.model.AuditAction;
import com.example.bwms.model.User;
import com.example.bwms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public AuthService(UserRepository userRepository, JwtService jwtService,
                       PasswordEncoder passwordEncoder, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    // Python: AuthService.authenticate
    private User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email.toLowerCase().strip())
                .orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getHashedPassword())) {
            log.warn("Failed login attempt for email={}", email);
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }
        return user;
    }

    // Python: AuthService.login — returns (token, user); @Transactional so audit log commits together
    @Transactional
    public LoginResult login(String email, String password) {
        User user = authenticate(email, password);
        String token = jwtService.generateToken(
                user.getId(), user.getRole().getValue(), user.getEmail());
        auditLogService.logAction(user.getId(), AuditAction.LOGIN,
                "user", user.getId(), "User " + user.getEmail() + " logged in");
        return new LoginResult(token, user);
    }

    public record LoginResult(String token, User user) {}
}
