// Translated from: backend/app/tests/conftest.py
// Replaces: SQLite engine/session fixtures → H2 in-memory via application-test.properties
//           seeded_users fixture → @BeforeEach insertUsers()
//           login() helper → getAuthToken()
//           auth_header() helper → authHeader()
package com.example.bwms;

import com.example.bwms.model.User;
import com.example.bwms.model.UserRole;
import com.example.bwms.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected TaskRepository taskRepository;
    @Autowired protected WorkflowEventRepository workflowEventRepository;
    @Autowired protected ApprovalRepository approvalRepository;
    @Autowired protected AuditLogRepository auditLogRepository;
    @Autowired protected PasswordEncoder passwordEncoder;

    protected User manager;
    protected User analyst;
    protected User otherAnalyst;

    @BeforeEach
    @Transactional
    public void cleanAndSeed() {
        // Delete in reverse FK dependency order to avoid constraint violations
        auditLogRepository.deleteAll();
        approvalRepository.deleteAll();
        workflowEventRepository.deleteAll();
        taskRepository.deleteAll();
        userRepository.deleteAll();

        manager = userRepository.save(new User(
                "Test Manager", "manager@test.com",
                passwordEncoder.encode("password123"), UserRole.MANAGER));
        analyst = userRepository.save(new User(
                "Test Analyst", "analyst@test.com",
                passwordEncoder.encode("password123"), UserRole.ANALYST));
        otherAnalyst = userRepository.save(new User(
                "Other Analyst", "other@test.com",
                passwordEncoder.encode("password123"), UserRole.ANALYST));
    }

    // Python: login() helper — returns the access_token string
    protected String getAuthToken(String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(
                new java.util.HashMap<>(java.util.Map.of("email", email, "password", password)));
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        return objectMapper.readTree(json).get("access_token").asText();
    }

    // Python: auth_header() helper
    protected String authHeader(String token) {
        return "Bearer " + token;
    }

    protected String managerToken() throws Exception {
        return getAuthToken("manager@test.com", "password123");
    }

    protected String analystToken() throws Exception {
        return getAuthToken("analyst@test.com", "password123");
    }
}
