// Translated from: backend/app/repositories/task_repository.py
package com.example.bwms.repository;

import com.example.bwms.model.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

// JpaSpecificationExecutor lets the service build dynamic filter queries with typed
// (non-null) predicates only — PostgreSQL cannot infer the type of a null bound parameter
// in JPQL like "WHERE (:status IS NULL OR t.status = :status)".
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    @Override
    @EntityGraph(attributePaths = {"assignee", "creator"})
    Optional<Task> findById(Long id);

    // Used by ReportService.tasks_by_status()
    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countByStatus();

    // Used by ReportService.tasks_by_priority()
    @Query("SELECT t.priority, COUNT(t) FROM Task t GROUP BY t.priority")
    List<Object[]> countByPriority();

    // Used by ReportService.user_workload()
    @Query("SELECT t.assignedToUserId, t.status, COUNT(t) FROM Task t GROUP BY t.assignedToUserId, t.status")
    List<Object[]> countByAssigneeAndStatus();

    // Used by ReportService.export_tasks_csv()
    @EntityGraph(attributePaths = {"assignee"})
    @Query("SELECT t FROM Task t ORDER BY t.id ASC")
    List<Task> findAllWithAssigneeOrderById();
}
