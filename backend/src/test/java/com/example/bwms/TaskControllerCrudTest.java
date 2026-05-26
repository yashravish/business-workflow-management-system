// Translated from: backend/app/tests/test_tasks_crud.py
package com.example.bwms;

import com.example.bwms.model.AuditAction;
import com.example.bwms.model.AuditLog;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskControllerCrudTest extends AbstractIntegrationTest {

    private JsonNode createTask(String token, String title, String priority) throws Exception {
        Map<String, Object> payload = Map.of(
                "title", title,
                "description", "Match invoices to GL",
                "priority", priority,
                "assigned_to_user_id", analyst.getId()
        );
        String resp = mockMvc.perform(post("/tasks")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp);
    }

    // Python: test_create_task_returns_201
    @Test
    void createTaskReturns201() throws Exception {
        String token = analystToken();
        JsonNode body = createTask(token, "Reconcile invoices", "medium");
        assertEquals("Reconcile invoices", body.get("title").asText());
        assertEquals("pending", body.get("status").asText());
        assertEquals("medium", body.get("priority").asText());
        assertEquals(analyst.getId(), body.get("assignee").get("id").asLong());
    }

    // Python: test_list_tasks
    @Test
    void listTasks() throws Exception {
        String token = analystToken();
        createTask(token, "A", "medium");
        createTask(token, "B", "medium");
        String resp = mockMvc.perform(get("/tasks").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode body = objectMapper.readTree(resp);
        assertEquals(2, body.size());
    }

    // Python: test_get_task_404
    @Test
    void getTask404() throws Exception {
        String token = analystToken();
        mockMvc.perform(get("/tasks/9999").header("Authorization", authHeader(token)))
                .andExpect(status().isNotFound());
    }

    // Python: test_update_task
    @Test
    void updateTask() throws Exception {
        String token = analystToken();
        JsonNode created = createTask(token, "Original", "medium");
        long taskId = created.get("id").asLong();

        String update = objectMapper.writeValueAsString(Map.of("title", "Updated title", "priority", "high"));
        String resp = mockMvc.perform(put("/tasks/" + taskId)
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode body = objectMapper.readTree(resp);
        assertEquals("Updated title", body.get("title").asText());
        assertEquals("high", body.get("priority").asText());
    }

    // Python: test_analyst_cannot_update_others_task
    @Test
    void analystCannotUpdateOthersTask() throws Exception {
        String manToken = managerToken();
        JsonNode created = createTask(manToken, "Manager's task", "medium");
        long taskId = created.get("id").asLong();

        String analToken = analystToken();
        mockMvc.perform(put("/tasks/" + taskId)
                        .header("Authorization", authHeader(analToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "hijacked"))))
                .andExpect(status().isForbidden());
    }

    // Python: test_delete_pending_task
    @Test
    void deletePendingTask() throws Exception {
        String token = analystToken();
        JsonNode created = createTask(token, "To delete", "medium");
        long taskId = created.get("id").asLong();

        mockMvc.perform(delete("/tasks/" + taskId).header("Authorization", authHeader(token)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/tasks/" + taskId).header("Authorization", authHeader(token)))
                .andExpect(status().isNotFound());
    }

    // Python: test_cannot_delete_approved_task
    @Test
    void cannotDeleteApprovedTask() throws Exception {
        String analToken = analystToken();
        String manToken = managerToken();
        JsonNode created = createTask(analToken, "Approve then try delete", "medium");
        long taskId = created.get("id").asLong();

        mockMvc.perform(post("/tasks/" + taskId + "/submit").header("Authorization", authHeader(analToken)));
        mockMvc.perform(post("/tasks/" + taskId + "/approve").header("Authorization", authHeader(manToken)));

        mockMvc.perform(delete("/tasks/" + taskId).header("Authorization", authHeader(analToken)))
                .andExpect(status().isBadRequest());
    }

    // Python: test_create_task_creates_audit_log
    @Test
    void createTaskCreatesAuditLog() throws Exception {
        String token = analystToken();
        createTask(token, "Audit me", "medium");

        List<AuditLog> logs = auditLogRepository.findAll();
        long createLogs = logs.stream().filter(l -> l.getAction() == AuditAction.CREATE_TASK).count();
        assertEquals(1, createLogs);
    }
}
