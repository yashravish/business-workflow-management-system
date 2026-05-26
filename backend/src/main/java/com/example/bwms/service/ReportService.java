// Translated from: backend/app/services/report_service.py
// NOTE: user_workload merges two queries in Java memory — same approach as the Python original.
//       See Change Log for discussion of a more efficient single-query alternative.
package com.example.bwms.service;

import com.example.bwms.dto.report.*;
import com.example.bwms.model.*;
import com.example.bwms.repository.ApprovalRepository;
import com.example.bwms.repository.TaskRepository;
import com.example.bwms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ReportService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ApprovalRepository approvalRepository;

    public ReportService(TaskRepository taskRepository, UserRepository userRepository,
                         ApprovalRepository approvalRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.approvalRepository = approvalRepository;
    }

    @Transactional(readOnly = true)
    public TasksByStatusReport tasksByStatus() {
        Map<TaskStatus, Integer> counts = new EnumMap<>(TaskStatus.class);
        for (TaskStatus s : TaskStatus.values()) counts.put(s, 0);

        for (Object[] row : taskRepository.countByStatus()) {
            TaskStatus status = (TaskStatus) row[0];
            counts.put(status, ((Long) row[1]).intValue());
        }

        List<StatusCountDto> items = new ArrayList<>();
        for (Map.Entry<TaskStatus, Integer> e : counts.entrySet()) {
            items.add(new StatusCountDto(e.getKey(), e.getValue()));
        }
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        return new TasksByStatusReport(items, total);
    }

    @Transactional(readOnly = true)
    public TasksByPriorityReport tasksByPriority() {
        Map<TaskPriority, Integer> counts = new EnumMap<>(TaskPriority.class);
        for (TaskPriority p : TaskPriority.values()) counts.put(p, 0);

        for (Object[] row : taskRepository.countByPriority()) {
            TaskPriority priority = (TaskPriority) row[0];
            counts.put(priority, ((Long) row[1]).intValue());
        }

        List<PriorityCountDto> items = new ArrayList<>();
        for (Map.Entry<TaskPriority, Integer> e : counts.entrySet()) {
            items.add(new PriorityCountDto(e.getKey(), e.getValue()));
        }
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        return new TasksByPriorityReport(items, total);
    }

    @Transactional(readOnly = true)
    public UserWorkloadReport userWorkload() {
        List<User> users = userRepository.findAllByOrderByNameAsc();

        // Initialise zero-count map for every user × status combination
        Map<Long, Map<TaskStatus, Integer>> perUser = new LinkedHashMap<>();
        for (User user : users) {
            Map<TaskStatus, Integer> statusMap = new EnumMap<>(TaskStatus.class);
            for (TaskStatus s : TaskStatus.values()) statusMap.put(s, 0);
            perUser.put(user.getId(), statusMap);
        }

        for (Object[] row : taskRepository.countByAssigneeAndStatus()) {
            Long userId = (Long) row[0];
            TaskStatus status = (TaskStatus) row[1];
            int count = ((Long) row[2]).intValue();
            if (perUser.containsKey(userId)) {
                perUser.get(userId).put(status, count);
            }
        }

        List<UserWorkloadEntry> items = new ArrayList<>();
        for (User user : users) {
            Map<TaskStatus, Integer> c = perUser.get(user.getId());
            int total = c.values().stream().mapToInt(Integer::intValue).sum();
            items.add(new UserWorkloadEntry(
                    user.getId(), user.getName(), user.getEmail(), user.getRole(),
                    total,
                    c.get(TaskStatus.PENDING),
                    c.get(TaskStatus.IN_PROGRESS),
                    c.get(TaskStatus.SUBMITTED),
                    c.get(TaskStatus.APPROVED),
                    c.get(TaskStatus.REJECTED),
                    c.get(TaskStatus.COMPLETED)
            ));
        }
        return new UserWorkloadReport(items);
    }

    @Transactional(readOnly = true)
    public ApprovalSummaryReport approvalSummary() {
        Map<ApprovalDecision, Integer> counts = new EnumMap<>(ApprovalDecision.class);
        for (ApprovalDecision d : ApprovalDecision.values()) counts.put(d, 0);

        for (Object[] row : approvalRepository.countByDecision()) {
            ApprovalDecision decision = (ApprovalDecision) row[0];
            counts.put(decision, ((Long) row[1]).intValue());
        }

        List<ApprovalSummaryEntry> items = new ArrayList<>();
        for (Map.Entry<ApprovalDecision, Integer> e : counts.entrySet()) {
            items.add(new ApprovalSummaryEntry(e.getKey(), e.getValue()));
        }
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        return new ApprovalSummaryReport(items, total);
    }

    @Transactional(readOnly = true)
    public String exportTasksCsv() {
        List<Task> tasks = taskRepository.findAllWithAssigneeOrderById();
        StringBuilder sb = new StringBuilder();
        sb.append("id,title,description,status,priority,assignee_name,assignee_email,created_by_user_id,created_at,updated_at\n");
        for (Task task : tasks) {
            User assignee = task.getAssignee();
            sb.append(csvRow(
                    String.valueOf(task.getId()),
                    task.getTitle(),
                    task.getDescription().replace("\n", " "),
                    task.getStatus().getValue(),
                    task.getPriority().getValue(),
                    assignee.getName(),
                    assignee.getEmail(),
                    String.valueOf(task.getCreatedByUserId()),
                    task.getCreatedAt() != null ? task.getCreatedAt().toString() : "",
                    task.getUpdatedAt() != null ? task.getUpdatedAt().toString() : ""
            ));
        }
        return sb.toString();
    }

    private String csvRow(String... fields) {
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) row.append(',');
            row.append(csvEscape(fields[i]));
        }
        row.append('\n');
        return row.toString();
    }

    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
