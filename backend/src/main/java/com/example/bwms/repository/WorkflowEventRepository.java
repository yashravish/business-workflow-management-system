// Translated from: backend/app/repositories/task_repository.py (list_workflow_events)
package com.example.bwms.repository;

import com.example.bwms.model.WorkflowEvent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowEventRepository extends JpaRepository<WorkflowEvent, Long> {

    @EntityGraph(attributePaths = {"changedBy"})
    List<WorkflowEvent> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
