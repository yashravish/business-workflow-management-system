// Translated from: backend/app/tests/test_permissions.py
package com.example.bwms;

import com.example.bwms.model.AuditAction;
import com.example.bwms.model.AuditLog;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PermissionsTest extends AbstractIntegrationTest {

    private long createAndSubmitTask(String analToken) throws Exception {
        Map<String, Object> payload = Map.of(
                "title", "Investigate AP discrepancy",
                "description", "",
                "priority", "medium",
                "assigned_to_user_id", analyst.getId()
        );
        String resp = mockMvc.perform(post("/tasks")
                        .header("Authorization", authHeader(analToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andReturn().getResponse().getContentAsString();
        long taskId = objectMapper.readTree(resp).get("id").asLong();
        mockMvc.perform(post("/tasks/" + taskId + "/submit")
                .header("Authorization", authHeader(analToken)));
        return taskId;
    }

    // Python: test_analyst_cannot_approve_task
    @Test
    void analystCannotApproveTask() throws Exception {
        String analToken = analystToken();
        long taskId = createAndSubmitTask(analToken);
        mockMvc.perform(post("/tasks/" + taskId + "/approve")
                        .header("Authorization", authHeader(analToken)))
                .andExpect(status().isForbidden());
    }

    // Python: test_analyst_cannot_reject_task
    @Test
    void analystCannotRejectTask() throws Exception {
        String analToken = analystToken();
        long taskId = createAndSubmitTask(analToken);
        mockMvc.perform(post("/tasks/" + taskId + "/reject")
                        .header("Authorization", authHeader(analToken)))
                .andExpect(status().isForbidden());
    }

    // Python: test_analyst_cannot_complete_task
    @Test
    void analystCannotCompleteTask() throws Exception {
        String analToken = analystToken();
        String manToken = managerToken();
        long taskId = createAndSubmitTask(analToken);
        mockMvc.perform(post("/tasks/" + taskId + "/approve")
                .header("Authorization", authHeader(manToken)));
        mockMvc.perform(post("/tasks/" + taskId + "/complete")
                        .header("Authorization", authHeader(analToken)))
                .andExpect(status().isForbidden());
    }

    // Python: test_manager_can_approve
    @Test
    void managerCanApprove() throws Exception {
        String analToken = analystToken();
        String manToken = managerToken();
        long taskId = createAndSubmitTask(analToken);
        mockMvc.perform(post("/tasks/" + taskId + "/approve")
                        .header("Authorization", authHeader(manToken)))
                .andExpect(status().isOk());
    }

    // Python: test_audit_log_records_approve_and_reject
    @Test
    void auditLogRecordsApproveAndReject() throws Exception {
        String analToken = analystToken();
        String manToken = managerToken();
        long taskId = createAndSubmitTask(analToken);
        mockMvc.perform(post("/tasks/" + taskId + "/approve")
                .header("Authorization", authHeader(manToken)));

        Set<AuditAction> actions = auditLogRepository.findAll().stream()
                .map(AuditLog::getAction)
                .collect(Collectors.toSet());
        assertTrue(actions.contains(AuditAction.SUBMIT_TASK));
        assertTrue(actions.contains(AuditAction.APPROVE_TASK));
    }
}
