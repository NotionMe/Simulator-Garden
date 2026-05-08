package ua.notion.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.notion.domain.entity.Garden;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.TestDatabaseManager;

class GardenServiceTest {
  private TestDatabaseManager testDbManager;
  private PersistenceContext context;
  private GardenService gardenService;
  private UserService userService;
  private Integer testUserId;

  @BeforeEach
  void setUp() throws Exception {
    testDbManager = new TestDatabaseManager();
    testDbManager.setup();
    context = new PersistenceContext(testDbManager.getConnectionPool());
    gardenService = new GardenService(context);
    userService = new UserService(context);

    User user = userService.createUser("testuser", "test@example.com");
    testUserId = user.getId();
  }

  @AfterEach
  void tearDown() {
    if (context != null) {
      context.close();
    }
    testDbManager.teardown();
  }

  @Test
  void testCreateGarden() {
    Garden garden = gardenService.createGarden(testUserId, "My Garden", 10, 10);

    assertNotNull(garden);
    assertNotNull(garden.getId());
    assertEquals(testUserId, garden.getUserId());
    assertEquals("My Garden", garden.getName());
    assertEquals(10, garden.getWidthCells());
    assertEquals(10, garden.getHeightCells());
    assertNotNull(garden.getCreatedAt());
  }

  @Test
  void testCreateGardenWithInvalidDimensions() {
    assertThrows(
        IllegalArgumentException.class,
        () -> gardenService.createGarden(testUserId, "Invalid", 0, 10));

    assertThrows(
        IllegalArgumentException.class,
        () -> gardenService.createGarden(testUserId, "Invalid", 10, -5));
  }

  @Test
  void testFindGardenById() {
    Garden created = gardenService.createGarden(testUserId, "My Garden", 10, 10);

    Optional<Garden> found = gardenService.findGardenById(created.getId());

    assertTrue(found.isPresent());
    assertEquals(created.getId(), found.get().getId());
    assertEquals("My Garden", found.get().getName());
  }

  @Test
  void testFindGardensByUserId() {
    gardenService.createGarden(testUserId, "Garden 1", 10, 10);
    gardenService.createGarden(testUserId, "Garden 2", 15, 15);

    List<Garden> gardens = gardenService.findGardensByUserId(testUserId);

    assertEquals(2, gardens.size());
  }

  @Test
  void testGetAllGardens() {
    gardenService.createGarden(testUserId, "Garden 1", 10, 10);
    gardenService.createGarden(testUserId, "Garden 2", 15, 15);

    List<Garden> gardens = gardenService.getAllGardens();

    assertEquals(2, gardens.size());
  }

  @Test
  void testUpdateGarden() {
    Garden garden = gardenService.createGarden(testUserId, "My Garden", 10, 10);
    garden.setName("Updated Garden");
    garden.setWidthCells(20);
    garden.setHeightCells(20);

    gardenService.updateGarden(garden);

    Optional<Garden> updated = gardenService.findGardenById(garden.getId());
    assertTrue(updated.isPresent());
    assertEquals("Updated Garden", updated.get().getName());
    assertEquals(20, updated.get().getWidthCells());
    assertEquals(20, updated.get().getHeightCells());
  }

  @Test
  void testUpdateGardenWithNullId() {
    Garden garden = Garden.builder().name("Test").widthCells(10).heightCells(10).build();

    assertThrows(IllegalArgumentException.class, () -> gardenService.updateGarden(garden));
  }

  @Test
  void testUpdateGardenWithInvalidDimensions() {
    Garden garden = gardenService.createGarden(testUserId, "My Garden", 10, 10);
    garden.setWidthCells(0);

    assertThrows(IllegalArgumentException.class, () -> gardenService.updateGarden(garden));
  }

  @Test
  void testDeleteGarden() {
    Garden garden = gardenService.createGarden(testUserId, "My Garden", 10, 10);
    Integer gardenId = garden.getId();

    gardenService.deleteGarden(gardenId);

    Optional<Garden> deleted = gardenService.findGardenById(gardenId);
    assertFalse(deleted.isPresent());
  }

  @Test
  void testGetTotalCells() {
    Garden garden = gardenService.createGarden(testUserId, "My Garden", 10, 15);

    int totalCells = gardenService.getTotalCells(garden);

    assertEquals(150, totalCells);
  }
}
