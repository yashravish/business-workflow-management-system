// Translated from: backend/app/tests/test_auth.py
package com.example.bwms;

import com.example.bwms.model.AuditAction;
import com.example.bwms.model.AuditLog;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends AbstractIntegrationTest {

    // Python: test_login_success
    @Test
    void loginSuccess() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "analyst@test.com", "password", "password123"));
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token_type").value("bearer"))
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("analyst@test.com"))
                .andExpect(jsonPath("$.user.role").value("analyst"));
    }

    // Python: test_login_wrong_password
    @Test
    void loginWrongPassword() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "analyst@test.com", "password", "wrongpassword"));
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    // Python: test_login_unknown_email
    @Test
    void loginUnknownEmail() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "ghost@test.com", "password", "password123"));
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    // Python: test_me_requires_authentication
    @Test
    void meRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // Python: test_me_returns_current_user
    @Test
    void meReturnsCurrentUser() throws Exception {
        String token = managerToken();
        mockMvc.perform(get("/auth/me").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("manager@test.com"))
                .andExpect(jsonPath("$.role").value("manager"));
    }

    // Python: test_protected_tasks_route_rejects_unauthenticated
    @Test
    void protectedTasksRouteRejectsUnauthenticated() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isUnauthorized());
    }

    // Python: test_login_creates_audit_log
    @Test
    void loginCreatesAuditLog() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "manager@test.com", "password", "password123"));
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        List<AuditLog> logs = auditLogRepository.findAll();
        assertTrue(logs.stream().anyMatch(l ->
                l.getAction() == AuditAction.LOGIN && manager.getId().equals(l.getUserId())));
    }
}
