package ua.notion.infrastructure.persistence.impl;

import java.util.List;
import ua.notion.domain.entity.Task;
import ua.notion.infrastructure.persistence.GenericRepository;
import ua.notion.infrastructure.persistence.contract.TaskRepository;
import ua.notion.infrastructure.persistence.util.ConnectionPool;

public class TaskRepositoryImpl extends GenericRepository<Task, Integer> implements TaskRepository {

  public TaskRepositoryImpl(ConnectionPool connectionPool) {
    super(connectionPool, Task.class, "tasks");
  }

  @Override
  public List<Task> findByPlantInstanceId(Integer plantInstanceId) {
    return findByField("plant_instance_id", plantInstanceId);
  }

  @Override
  public List<Task> findByTaskType(String taskType) {
    return findByField("task_type", taskType);
  }

  @Override
  public List<Task> findPendingTasks() {
    Filter filter =
        (whereClause, params) -> {
          whereClause.add("is_done = ?");
          params.add(false);
        };
    return findAll(filter, "due_at", true, 0, Integer.MAX_VALUE);
  }

  @Override
  public long countByPlantInstanceId(Integer plantInstanceId) {
    Filter filter =
        (whereClause, params) -> {
          whereClause.add("plant_instance_id = ?");
          params.add(plantInstanceId);
        };
    return count(filter);
  }
}
