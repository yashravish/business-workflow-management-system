// Translated from: backend/app/tests/test_reports.py
package com.example.bwms;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReportControllerTest extends AbstractIntegrationTest {

    private long createTask(String token, String title, String priority) throws Exception {
        Map<String, Object> payload = Map.of(
                "title", title, "description", "",
                "priority", priority, "assigned_to_user_id", analyst.getId()
        );
        String resp = mockMvc.perform(post("/tasks")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asLong();
    }

    // Python: test_tasks_by_status_report
    @Test
    void tasksByStatusReport() throws Exception {
        String analToken = analystToken();
        createTask(analToken, "t1", "medium");
        long t2 = createTask(analToken, "t2", "medium");
        mockMvc.perform(post("/tasks/" + t2 + "/submit").header("Authorization", authHeader(analToken)));

        String resp = mockMvc.perform(get("/reports/tasks-by-status")
                        .header("Authorization", authHeader(analToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode body = objectMapper.readTree(resp);

        int pending = 0, submitted = 0;
        for (JsonNode item : body.get("items")) {
            if ("pending".equals(item.get("status").asText()))   pending   = item.get("count").asInt();
            if ("submitted".equals(item.get("status").asText())) submitted = item.get("count").asInt();
        }
        assertEquals(1, pending);
        assertEquals(1, submitted);
        assertEquals(2, body.get("total").asInt());
    }

    // Python: test_tasks_by_priority_report
    @Test
    void tasksByPriorityReport() throws Exception {
        String analToken = analystToken();
        createTask(analToken, "a", "high");
        createTask(analToken, "b", "high");
        createTask(analToken, "c", "low");

        String resp = mockMvc.perform(get("/reports/tasks-by-priority")
                        .header("Authorization", authHeader(analToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode items = objectMapper.readTree(resp).get("items");

        int high = 0, low = 0, medium = 0;
        for (JsonNode item : items) {
            switch (item.get("priority").asText()) {
                case "high"   -> high   = item.get("count").asInt();
                case "low"    -> low    = item.get("count").asInt();
                case "medium" -> medium = item.get("count").asInt();
            }
        }
        assertEquals(2, high);
        assertEquals(1, low);
        assertEquals(0, medium);
    }

    // Python: test_user_workload_report
    @Test
    void userWorkloadReport() throws Exception {
        String analToken = analystToken();
        createTask(analToken, "x", "medium");
        createTask(analToken, "y", "medium");

        String resp = mockMvc.perform(get("/reports/user-workload")
                        .header("Authorization", authHeader(analToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode items = objectMapper.readTree(resp).get("items");

        JsonNode analystEntry = null;
        for (JsonNode item : items) {
            if (analyst.getId().equals(item.get("user_id").asLong())) {
                analystEntry = item;
                break;
            }
        }
        assertNotNull(analystEntry);
        assertEquals(2, analystEntry.get("total").asInt());
        assertEquals(2, analystEntry.get("pending").asInt());
    }

    // Python: test_approval_summary_report
    @Test
    void approvalSummaryReport() throws Exception {
        String analToken = analystToken();
        String manToken = managerToken();

        long t1 = createTask(analToken, "approve me", "medium");
        mockMvc.perform(post("/tasks/" + t1 + "/submit").header("Authorization", authHeader(analToken)));
        mockMvc.perform(post("/tasks/" + t1 + "/approve").header("Authorization", authHeader(manToken)));

        long t2 = createTask(analToken, "reject me", "medium");
        mockMvc.perform(post("/tasks/" + t2 + "/submit").header("Authorization", authHeader(analToken)));
        mockMvc.perform(post("/tasks/" + t2 + "/reject").header("Authorization", authHeader(manToken)));

        String resp = mockMvc.perform(get("/reports/approval-summary")
                        .header("Authorization", authHeader(analToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode body = objectMapper.readTree(resp);

        int approved = 0, rejected = 0;
        for (JsonNode item : body.get("items")) {
            if ("approved".equals(item.get("decision").asText())) approved = item.get("count").asInt();
            if ("rejected".equals(item.get("decision").asText())) rejected = item.get("count").asInt();
        }
        assertEquals(1, approved);
        assertEquals(1, rejected);
        assertEquals(2, body.get("total").asInt());
    }

    // Python: test_csv_export
    @Test
    void csvExport() throws Exception {
        String analToken = analystToken();
        createTask(analToken, "csv item", "medium");

        String resp = mockMvc.perform(get("/reports/export/tasks.csv")
                        .header("Authorization", authHeader(analToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String[] lines = resp.split("\n");
        assertTrue(lines[0].contains("id") && lines[0].contains("title"),
                "First line should be CSV header");
        assertTrue(resp.contains("csv item"), "CSV body should contain the task title");
    }

    // Python: test_audit_logs_endpoint
    @Test
    void auditLogsEndpoint() throws Exception {
        String analToken = analystToken();
        createTask(analToken, "any task", "medium");

        String resp = mockMvc.perform(get("/audit-logs")
                        .header("Authorization", authHeader(analToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode logs = objectMapper.readTree(resp);
        assertTrue(logs.isArray());
        boolean hasCreateTask = false;
        for (JsonNode log : logs) {
            if ("create_task".equals(log.get("action").asText())) { hasCreateTask = true; break; }
        }
        assertTrue(hasCreateTask);
    }
}
