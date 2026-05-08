package ua.notion.domain.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import ua.notion.domain.entity.Task;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.contract.TaskRepository;

public class TaskService {
  private final PersistenceContext context;
  private final TaskRepository taskRepository;

  public TaskService(PersistenceContext context) {
    this.context = context;
    this.taskRepository = context.getTaskRepository();
  }

  public Task createTask(Integer plantInstanceId, String taskType, LocalDateTime dueAt) {
    Task task =
        Task.builder()
            .plantInstanceId(plantInstanceId)
            .taskType(taskType)
            .dueAt(dueAt)
            .isDone(false)
            .build();

    context.beginTransaction();
    try {
      taskRepository.save(task);
      context.commitTransaction();
      return task;
    } catch (Exception e) {
      context.rollbackTransaction();
      throw new RuntimeException("Failed to create task", e);
    }
  }

  public Optional<Task> findTaskById(Integer id) {
    return taskRepository.findById(id);
  }

  public List<Task> findTasksByPlantInstanceId(Integer plantInstanceId) {
    return taskRepository.findAll().stream()
        .filter(t -> t.getPlantInstanceId().equals(plantInstanceId))
        .toList();
  }

  public List<Task> findPendingTasks() {
    return taskRepository.findAll().stream().filter(t -> !t.getIsDone()).toList();
  }

  public List<Task> findOverdueTasks() {
    LocalDateTime now = LocalDateTime.now();
    return taskRepository.findAll().stream()
        .filter(t -> !t.getIsDone() && t.getDueAt().isBefore(now))
        .toList();
  }

  public List<Task> findTasksByType(String taskType) {
    return taskRepository.findAll().stream()
        .filter(t -> t.getTaskType().equalsIgnoreCase(taskType))
        .toList();
  }

  public void completeTask(Integer taskId) {
    Optional<Task> taskOpt = taskRepository.findById(taskId);
    if (taskOpt.isEmpty()) {
      throw new IllegalArgumentException("Task not found");
    }

    Task task = taskOpt.get();
    task.setIsDone(true);

    context.beginTransaction();
    try {
      taskRepository.update(task.getId(), task);
      context.commitTransaction();
    } catch (Exception e) {
      context.rollbackTransaction();
      throw new RuntimeException("Failed to complete task", e);
    }
  }

  public void updateTask(Task task) {
    if (task.getId() == null) {
      throw new IllegalArgumentException("Task ID cannot be null");
    }

    context.beginTransaction();
    try {
      taskRepository.update(task.getId(), task);
      context.commitTransaction();
    } catch (Exception e) {
      context.rollbackTransaction();
      throw new RuntimeException("Failed to update task", e);
    }
  }

  public void deleteTask(Integer id) {
    context.beginTransaction();
    try {
      taskRepository.delete(id);
      context.commitTransaction();
    } catch (Exception e) {
      context.rollbackTransaction();
      throw new RuntimeException("Failed to delete task", e);
    }
  }

  public long countPendingTasks() {
    return taskRepository.findAll().stream().filter(t -> !t.getIsDone()).count();
  }

  public long countOverdueTasks() {
    LocalDateTime now = LocalDateTime.now();
    return taskRepository.findAll().stream()
        .filter(t -> !t.getIsDone() && t.getDueAt().isBefore(now))
        .count();
  }
}
