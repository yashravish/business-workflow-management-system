// Translated from: backend/app/tests/test_workflow.py
package com.example.bwms;

import com.example.bwms.model.TaskStatus;
import com.example.bwms.service.WorkflowService;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.EnumSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WorkflowTest extends AbstractIntegrationTest {

    private long createTask(String token, String title) throws Exception {
        Map<String, Object> payload = Map.of(
                "title", title, "description", "",
                "priority", "high", "assigned_to_user_id", analyst.getId()
        );
        String resp = mockMvc.perform(post("/tasks")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asLong();
    }

    // Python: test_valid_transitions_table_matches_spec
    @Test
    void validTransitionsTableMatchesSpec() {
        assertEquals(EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.SUBMITTED),
                WorkflowService.VALID_TRANSITIONS.get(TaskStatus.PENDING));
        assertEquals(EnumSet.of(TaskStatus.SUBMITTED),
                WorkflowService.VALID_TRANSITIONS.get(TaskStatus.IN_PROGRESS));
        assertEquals(EnumSet.of(TaskStatus.APPROVED, TaskStatus.REJECTED),
                WorkflowService.VALID_TRANSITIONS.get(TaskStatus.SUBMITTED));
        assertEquals(EnumSet.of(TaskStatus.COMPLETED),
                WorkflowService.VALID_TRANSITIONS.get(TaskStatus.APPROVED));
        assertEquals(EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.SUBMITTED),
                WorkflowService.VALID_TRANSITIONS.get(TaskStatus.REJECTED));
        assertEquals(EnumSet.noneOf(TaskStatus.class),
                WorkflowService.VALID_TRANSITIONS.get(TaskStatus.COMPLETED));
    }

    // Python: test_is_valid_transition_helper
    @Test
    void isValidTransitionHelper() {
        assertTrue(WorkflowService.isValidTransition(TaskStatus.PENDING, TaskStatus.SUBMITTED));
        assertFalse(WorkflowService.isValidTransition(TaskStatus.PENDING, TaskStatus.COMPLETED));
        assertFalse(WorkflowService.isValidTransition(TaskStatus.COMPLETED, TaskStatus.PENDING));
    }

    // Python: test_submit_then_approve_full_flow
    @Test
    void submitThenApproveFullFlow() throws Exception {
        String analToken = analystToken();
        String manToken = managerToken();
        long taskId = createTask(analToken, "Audit accounts");

        mockMvc.perform(post("/tasks/" + taskId + "/submit")
                        .header("Authorization", authHeader(analToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("submitted"));

        mockMvc.perform(post("/tasks/" + taskId + "/approve")
                        .header("Authorization", authHeader(manToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("comment", "looks good"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("approved"));

        mockMvc.perform(post("/tasks/" + taskId + "/complete")
                        .header("Authorization", authHeader(manToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("completed"));
    }

    // Python: test_rejected_task_can_be_resubmitted
    @Test
    void rejectedTaskCanBeResubmitted() throws Exception {
        String analToken = analystToken();
        String manToken = managerToken();
        long taskId = createTask(analToken, "Audit accounts");
        mockMvc.perform(post("/tasks/" + taskId + "/submit")
                .header("Authorization", authHeader(analToken)));
        mockMvc.perform(post("/tasks/" + taskId + "/reject")
                        .header("Authorization", authHeader(manToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("comment", "Need more detail"))))
                .andExpect(jsonPath("$.status").value("rejected"));
        mockMvc.perform(post("/tasks/" + taskId + "/submit")
                        .header("Authorization", authHeader(analToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("submitted"));
    }

    // Python: test_cannot_complete_pending_task
    @Test
    void cannotCompletePendingTask() throws Exception {
        long taskId = createTask(managerToken(), "pending task");
        mockMvc.perform(post("/tasks/" + taskId + "/complete")
                        .header("Authorization", authHeader(managerToken())))
                .andExpect(status().isBadRequest());
    }

    // Python: test_cannot_approve_pending_task
    @Test
    void cannotApprovePendingTask() throws Exception {
        long taskId = createTask(analystToken(), "pending task");
        mockMvc.perform(post("/tasks/" + taskId + "/approve")
                        .header("Authorization", authHeader(managerToken())))
                .andExpect(status().isBadRequest());
    }

    // Python: test_cannot_edit_completed_task
    @Test
    void cannotEditCompletedTask() throws Exception {
        String analToken = analystToken();
        String manToken = managerToken();
        long taskId = createTask(analToken, "To complete");
        mockMvc.perform(post("/tasks/" + taskId + "/submit").header("Authorization", authHeader(analToken)));
        mockMvc.perform(post("/tasks/" + taskId + "/approve").header("Authorization", authHeader(manToken)));
        mockMvc.perform(post("/tasks/" + taskId + "/complete").header("Authorization", authHeader(manToken)));

        mockMvc.perform(put("/tasks/" + taskId)
                        .header("Authorization", authHeader(manToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "should fail"))))
                .andExpect(status().isBadRequest());
    }

    // Python: test_workflow_event_history_endpoint
    @Test
    void workflowEventHistoryEndpoint() throws Exception {
        String analToken = analystToken();
        String manToken = managerToken();
        long taskId = createTask(analToken, "Audit accounts");
        mockMvc.perform(post("/tasks/" + taskId + "/submit").header("Authorization", authHeader(analToken)));
        mockMvc.perform(post("/tasks/" + taskId + "/approve").header("Authorization", authHeader(manToken)));

        String resp = mockMvc.perform(get("/tasks/" + taskId + "/workflow-events")
                        .header("Authorization", authHeader(analToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode events = objectMapper.readTree(resp);

        boolean hasCreation  = false;
        boolean hasPendingToSubmitted = false;
        boolean hasSubmittedToApproved = false;
        for (JsonNode e : events) {
            String from = e.get("from_status").isNull() ? null : e.get("from_status").asText();
            String to = e.get("to_status").asText();
            if (from == null && "pending".equals(to)) hasCreation = true;
            if ("pending".equals(from) && "submitted".equals(to)) hasPendingToSubmitted = true;
            if ("submitted".equals(from) && "approved".equals(to)) hasSubmittedToApproved = true;
        }
        assertTrue(hasCreation);
        assertTrue(hasPendingToSubmitted);
        assertTrue(hasSubmittedToApproved);
    }
}
