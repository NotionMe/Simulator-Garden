package ua.notion.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import ua.notion.domain.entity.Garden;
import ua.notion.domain.entity.Plant;
import ua.notion.domain.entity.PlantInstance;
import ua.notion.domain.entity.Task;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.contract.GardenRepository;
import ua.notion.infrastructure.persistence.contract.PlantInstanceRepository;
import ua.notion.infrastructure.persistence.contract.PlantRepository;
import ua.notion.infrastructure.persistence.contract.TaskRepository;
import ua.notion.infrastructure.persistence.contract.UserRepository;

class TaskRepositoryTest extends BaseRepositoryTest {

  @Test
  void shouldSaveAndFindTaskById() {
    TaskRepository taskRepo = context.getTaskRepository();
    PlantInstance instance = createPlantInstance();

    Task task =
        Task.builder()
            .plantInstanceId(instance.getId())
            .taskType("water")
            .dueAt(LocalDateTime.now().plusDays(1))
            .isDone(false)
            .build();

    Task saved = taskRepo.save(task);

    assertNotNull(saved.getId());
    assertEquals("water", saved.getTaskType());
    assertFalse(saved.getIsDone());

    Optional<Task> found = taskRepo.findById(saved.getId());
    assertTrue(found.isPresent());
    assertEquals("water", found.get().getTaskType());
  }

  @Test
  void shouldUpdateTask() {
    TaskRepository taskRepo = context.getTaskRepository();
    PlantInstance instance = createPlantInstance();

    Task task =
        taskRepo.save(
            Task.builder()
                .plantInstanceId(instance.getId())
                .taskType("fertilize")
                .dueAt(LocalDateTime.now().plusDays(1))
                .isDone(false)
                .build());

    task.setIsDone(true);
    task.setTaskType("harvest");

    taskRepo.update(task.getId(), task);

    Optional<Task> updated = taskRepo.findById(task.getId());
    assertTrue(updated.isPresent());
    assertTrue(updated.get().getIsDone());
    assertEquals("harvest", updated.get().getTaskType());
  }

  @Test
  void shouldDeleteTask() {
    TaskRepository taskRepo = context.getTaskRepository();
    PlantInstance instance = createPlantInstance();

    Task task =
        taskRepo.save(
            Task.builder()
                .plantInstanceId(instance.getId())
                .taskType("prune")
                .dueAt(LocalDateTime.now().plusDays(1))
                .isDone(false)
                .build());

    Integer id = task.getId();
    taskRepo.delete(id);

    Optional<Task> deleted = taskRepo.findById(id);
    assertFalse(deleted.isPresent());
  }

  @Test
  void shouldEnforceTaskTypeConstraint() {
    TaskRepository taskRepo = context.getTaskRepository();
    PlantInstance instance = createPlantInstance();

    assertThrows(
        Exception.class,
        () ->
            taskRepo.save(
                Task.builder()
                    .plantInstanceId(instance.getId())
                    .taskType("invalid_type")
                    .dueAt(LocalDateTime.now().plusDays(1))
                    .isDone(false)
                    .build()));
  }

  @Test
  void shouldCascadeDeleteWhenPlantInstanceDeleted() {
    TaskRepository taskRepo = context.getTaskRepository();
    PlantInstanceRepository instanceRepo = context.getPlantInstanceRepository();
    PlantInstance instance = createPlantInstance();

    Task task =
        taskRepo.save(
            Task.builder()
                .plantInstanceId(instance.getId())
                .taskType("water")
                .dueAt(LocalDateTime.now().plusDays(1))
                .isDone(false)
                .build());

    Integer taskId = task.getId();
    instanceRepo.delete(instance.getId());

    Optional<Task> deleted = taskRepo.findById(taskId);
    assertFalse(deleted.isPresent());
  }

  @Test
  void shouldFindTasksByPlantInstanceId() {
    TaskRepository taskRepo = context.getTaskRepository();
    PlantInstance instance1 = createPlantInstance();
    PlantInstance instance2 = createPlantInstance();

    taskRepo.save(
        Task.builder()
            .plantInstanceId(instance1.getId())
            .taskType("water")
            .dueAt(LocalDateTime.now().plusDays(1))
            .isDone(false)
            .build());

    taskRepo.save(
        Task.builder()
            .plantInstanceId(instance1.getId())
            .taskType("fertilize")
            .dueAt(LocalDateTime.now().plusDays(2))
            .isDone(false)
            .build());

    taskRepo.save(
        Task.builder()
            .plantInstanceId(instance2.getId())
            .taskType("harvest")
            .dueAt(LocalDateTime.now().plusDays(3))
            .isDone(false)
            .build());

    List<Task> instance1Tasks = taskRepo.findByField("plant_instance_id", instance1.getId());
    assertEquals(2, instance1Tasks.size());
  }

  @Test
  void shouldFindTasksByStatus() {
    TaskRepository taskRepo = context.getTaskRepository();
    PlantInstance instance = createPlantInstance();

    taskRepo.save(
        Task.builder()
            .plantInstanceId(instance.getId())
            .taskType("water")
            .dueAt(LocalDateTime.now().plusDays(1))
            .isDone(false)
            .build());

    taskRepo.save(
        Task.builder()
            .plantInstanceId(instance.getId())
            .taskType("fertilize")
            .dueAt(LocalDateTime.now().plusDays(2))
            .isDone(true)
            .build());

    taskRepo.save(
        Task.builder()
            .plantInstanceId(instance.getId())
            .taskType("harvest")
            .dueAt(LocalDateTime.now().plusDays(3))
            .isDone(false)
            .build());

    List<Task> pendingTasks = taskRepo.findByField("is_done", false);
    assertEquals(2, pendingTasks.size());

    List<Task> completedTasks = taskRepo.findByField("is_done", true);
    assertEquals(1, completedTasks.size());
  }

  @Test
  void shouldCountTasks() {
    TaskRepository taskRepo = context.getTaskRepository();
    PlantInstance instance = createPlantInstance();

    taskRepo.save(
        Task.builder()
            .plantInstanceId(instance.getId())
            .taskType("water")
            .dueAt(LocalDateTime.now().plusDays(1))
            .isDone(false)
            .build());

    taskRepo.save(
        Task.builder()
            .plantInstanceId(instance.getId())
            .taskType("fertilize")
            .dueAt(LocalDateTime.now().plusDays(2))
            .isDone(false)
            .build());

    long count = taskRepo.count();
    assertEquals(2, count);
  }

  @Test
  void shouldHandleAllTaskTypes() {
    TaskRepository taskRepo = context.getTaskRepository();
    PlantInstance instance = createPlantInstance();

    String[] taskTypes = {"water", "fertilize", "harvest", "prune"};

    for (String type : taskTypes) {
      Task task =
          taskRepo.save(
              Task.builder()
                  .plantInstanceId(instance.getId())
                  .taskType(type)
                  .dueAt(LocalDateTime.now().plusDays(1))
                  .isDone(false)
                  .build());

      assertNotNull(task.getId());
      assertEquals(type, task.getTaskType());
    }

    long count = taskRepo.count();
    assertEquals(4, count);
  }

  private PlantInstance createPlantInstance() {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();
    PlantRepository plantRepo = context.getPlantRepository();
    PlantInstanceRepository instanceRepo = context.getPlantInstanceRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user" + System.nanoTime())
                .email("user" + System.nanoTime() + "@example.com")
                .createdAt(LocalDateTime.now())
                .build());

    Garden garden =
        gardenRepo.save(
            Garden.builder()
                .userId(user.getId())
                .name("Garden")
                .widthCells(10)
                .heightCells(10)
                .createdAt(LocalDateTime.now())
                .build());

    Plant plant =
        plantRepo.save(
            Plant.builder()
                .name("Plant")
                .species("Species")
                .growthDays(30)
                .climateType("temperate")
                .build());

    return instanceRepo.save(
        PlantInstance.builder()
            .gardenId(garden.getId())
            .plantId(plant.getId())
            .cellX((int) (Math.random() * 10))
            .cellY((int) (Math.random() * 10))
            .plantedAt(LocalDate.now())
            .growthStage(0)
            .isWatered(false)
            .isFertilized(false)
            .build());
  }
}
