package ua.notion.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.notion.domain.entity.PlantInstance;
import ua.notion.domain.entity.Task;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.TestDatabaseManager;

class TaskServiceTest {
  private TestDatabaseManager testDbManager;
  private PersistenceContext context;
  private TaskService taskService;
  private GardenService gardenService;
  private PlantService plantService;
  private PlantInstanceService plantInstanceService;
  private UserService userService;
  private Integer testPlantInstanceId;

  @BeforeEach
  void setUp() throws Exception {
    testDbManager = new TestDatabaseManager();
    testDbManager.setup();
    context = new PersistenceContext(testDbManager.getConnectionPool());

    taskService = new TaskService(context);
    gardenService = new GardenService(context);
    plantService = new PlantService(context);
    plantInstanceService = new PlantInstanceService(context);
    userService = new UserService(context);

    // Create test data
    User user = userService.createUser("testuser", "test@example.com");
    var garden = gardenService.createGarden(user.getId(), "Test Garden", 10, 10);
    var plant = plantService.createPlant("Tomato", "Solanum", 60, "temperate", "tomato");
    PlantInstance instance = plantInstanceService.plantSeed(garden.getId(), plant.getId(), 5, 5);
    testPlantInstanceId = instance.getId();
  }

  @AfterEach
  void tearDown() {
    if (context != null) {
      context.close();
    }
    testDbManager.teardown();
  }

  @Test
  void testCreateTask() {
    LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
    Task task = taskService.createTask(testPlantInstanceId, "water", dueDate);

    assertNotNull(task);
    assertNotNull(task.getId());
    assertEquals(testPlantInstanceId, task.getPlantInstanceId());
    assertEquals("water", task.getTaskType());
    assertNotNull(task.getDueAt());
    assertFalse(task.getIsDone());

    System.out.println("Created task with due_at: " + task.getDueAt());
  }

  @Test
  void testCreateTaskWithDoneStatus() {
    LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
    Task task = taskService.createTask(testPlantInstanceId, "fertilize", dueDate);

    // Update to done
    task.setIsDone(true);
    taskService.updateTask(task);

    var found = taskService.findTaskById(task.getId());
    assertTrue(found.isPresent());
    assertTrue(found.get().getIsDone());
  }

  @Test
  void testFindPendingTasks() {
    taskService.createTask(testPlantInstanceId, "water", LocalDateTime.now().plusDays(1));
    taskService.createTask(testPlantInstanceId, "fertilize", LocalDateTime.now().plusDays(2));

    var pending = taskService.findPendingTasks();
    assertEquals(2, pending.size());
  }
}
