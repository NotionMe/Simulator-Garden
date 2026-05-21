package ua.notion.infrastructure.persistence.impl;

import java.util.Comparator;
import java.util.List;
import ua.notion.domain.entity.Task;
import ua.notion.infrastructure.persistence.GenericRepository;
import ua.notion.infrastructure.persistence.contract.TaskRepository;
import ua.notion.infrastructure.websocket.WebSocketApiClient;

public class TaskRepositoryImpl extends GenericRepository<Task, Integer> implements TaskRepository {

  public TaskRepositoryImpl(WebSocketApiClient apiClient) {
    super(apiClient, Task.class, "task");
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
    return findAll().stream()
        .filter(t -> !Boolean.TRUE.equals(t.getIsDone()))
        .sorted(
            Comparator.comparing(Task::getDueAt, Comparator.nullsLast(Comparator.naturalOrder())))
        .toList();
  }

  @Override
  public long countByPlantInstanceId(Integer plantInstanceId) {
    return findByPlantInstanceId(plantInstanceId).size();
  }
}
