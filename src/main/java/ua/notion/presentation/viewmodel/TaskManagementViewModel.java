package ua.notion.presentation.viewmodel;

import com.google.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ua.notion.domain.entity.Task;
import ua.notion.domain.service.TaskService;

public class TaskManagementViewModel {

  private static final System.Logger logger =
      System.getLogger(TaskManagementViewModel.class.getName());

  private final TaskService taskService;
  private final ObservableList<Task> tasks = FXCollections.observableArrayList();
  private final StringProperty statusFilter = new SimpleStringProperty("All Tasks");
  private final ObjectProperty<java.time.LocalDate> dueDateFilter = new SimpleObjectProperty<>();
  private final StringProperty errorMessage = new SimpleStringProperty();
  private final StringProperty successMessage = new SimpleStringProperty();

  @Inject
  public TaskManagementViewModel(TaskService taskService) {
    this.taskService = taskService;
    logger.log(System.Logger.Level.INFO, "TaskManagementViewModel initialized");
  }

  public void loadTasks() {
    try {
      logger.log(System.Logger.Level.INFO, "Loading all tasks...");
      List<Task> allTasks = taskService.findTasksByPlantInstanceId(null);
      logger.log(System.Logger.Level.INFO, "Loaded {0} tasks", allTasks.size());
      tasks.setAll(allTasks);
      clearMessages();
    } catch (Exception e) {
      logger.log(System.Logger.Level.ERROR, "Failed to load tasks", e);
      errorMessage.set("Failed to load tasks: " + e.getMessage());
    }
  }

  public void applyFilters() {
    try {
      List<Task> filtered;
      String status = statusFilter.get();

      if ("Pending".equals(status)) {
        filtered = taskService.findPendingTasks();
      } else if ("Done".equals(status)) {
        filtered =
            taskService.findTasksByPlantInstanceId(null).stream().filter(Task::getIsDone).toList();
      } else {
        filtered = taskService.findTasksByPlantInstanceId(null);
      }

      if (dueDateFilter.get() != null) {
        java.time.LocalDate filterDate = dueDateFilter.get();
        filtered =
            filtered.stream()
                .filter(
                    task ->
                        task.getDueAt() != null && task.getDueAt().toLocalDate().equals(filterDate))
                .toList();
      }

      tasks.setAll(filtered);
      clearMessages();
    } catch (Exception e) {
      errorMessage.set("Failed to apply filters: " + e.getMessage());
    }
  }

  public void clearFilters() {
    statusFilter.set("All Tasks");
    dueDateFilter.set(null);
    loadTasks();
  }

  public void createTask(
      Integer plantInstanceId, String taskType, LocalDateTime dueAt, Boolean isDone) {
    try {
      logger.log(
          System.Logger.Level.INFO,
          "Creating task: plantInstanceId={0}, taskType={1}, dueAt={2}, isDone={3}",
          plantInstanceId,
          taskType,
          dueAt,
          isDone);
      Task task = taskService.createTask(plantInstanceId, taskType, dueAt);
      logger.log(System.Logger.Level.INFO, "Task created with ID: {0}", task.getId());
      if (isDone != null && isDone) {
        logger.log(System.Logger.Level.INFO, "Setting task as done");
        task.setIsDone(true);
        taskService.updateTask(task);
      }
      successMessage.set("Task created successfully!");
      loadTasks();
    } catch (Exception e) {
      logger.log(System.Logger.Level.ERROR, "Failed to create task", e);
      errorMessage.set("Failed to create task: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void updateTask(Task task) {
    try {
      logger.log(System.Logger.Level.INFO, "Updating task ID: {0}", task.getId());
      taskService.updateTask(task);
      logger.log(System.Logger.Level.INFO, "Task updated successfully");
      successMessage.set("Task updated successfully!");
      loadTasks();
    } catch (Exception e) {
      logger.log(System.Logger.Level.ERROR, "Failed to update task", e);
      errorMessage.set("Failed to update task: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void toggleTaskStatus(Task task) {
    try {
      logger.log(
          System.Logger.Level.INFO,
          "Toggling task status for ID: {0}, current status: {1}",
          task.getId(),
          task.getIsDone());
      task.setIsDone(!task.getIsDone());
      taskService.updateTask(task);
      logger.log(System.Logger.Level.INFO, "Task status toggled to: {0}", task.getIsDone());
      successMessage.set("Task status updated successfully!");
      loadTasks();
    } catch (Exception e) {
      logger.log(System.Logger.Level.ERROR, "Failed to update task status", e);
      errorMessage.set("Failed to update task status: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void deleteTask(Integer taskId) {
    try {
      logger.log(System.Logger.Level.INFO, "Deleting task ID: {0}", taskId);
      taskService.deleteTask(taskId);
      logger.log(System.Logger.Level.INFO, "Task deleted successfully");
      successMessage.set("Task deleted successfully!");
      loadTasks();
    } catch (Exception e) {
      logger.log(System.Logger.Level.ERROR, "Failed to delete task", e);
      errorMessage.set("Failed to delete task: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void clearMessages() {
    errorMessage.set(null);
    successMessage.set(null);
  }

  // Getters for properties
  public ObservableList<Task> getTasks() {
    return tasks;
  }

  public StringProperty statusFilterProperty() {
    return statusFilter;
  }

  public ObjectProperty<java.time.LocalDate> dueDateFilterProperty() {
    return dueDateFilter;
  }

  public StringProperty errorMessageProperty() {
    return errorMessage;
  }

  public StringProperty successMessageProperty() {
    return successMessage;
  }
}
