package ua.notion.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.notion.domain.entity.Garden;
import ua.notion.domain.entity.Plant;
import ua.notion.domain.entity.PlantInstance;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.TestDatabaseManager;

class PlantInstanceServiceTest {
  private TestDatabaseManager dbManager;
  private PersistenceContext context;
  private PlantInstanceService plantInstanceService;
  private UserService userService;
  private GardenService gardenService;
  private PlantService plantService;

  private User testUser;
  private Garden testGarden;
  private Plant testPlant;

  @BeforeEach
  void setUp() {
    dbManager = new TestDatabaseManager();
    context = dbManager.createContext();
    plantInstanceService = new PlantInstanceService(context);
    userService = new UserService(context);
    gardenService = new GardenService(context);
    plantService = new PlantService(context);

    testUser = userService.createUser("testuser", "test@example.com");
    testGarden = gardenService.createGarden(testUser.getId(), "Test Garden", 10, 10);
    testPlant =
        plantService.createPlant("Tomato", "Solanum lycopersicum", 60, "tropical", "tomato");
  }

  @AfterEach
  void tearDown() {
    if (context != null) {
      context.close();
    }
    dbManager.cleanup();
  }

  @Test
  void testPlantSeed() {
    PlantInstance instance =
        plantInstanceService.plantSeed(testGarden.getId(), testPlant.getId(), 5, 5);

    assertNotNull(instance);
    assertNotNull(instance.getId());
    assertEquals(testGarden.getId(), instance.getGardenId());
    assertEquals(testPlant.getId(), instance.getPlantId());
    assertEquals(5, instance.getCellX());
    assertEquals(5, instance.getCellY());
    assertEquals(0, instance.getGrowthStage());
    assertFalse(instance.getIsWatered());
    assertFalse(instance.getIsFertilized());
    assertEquals(LocalDate.now(), instance.getPlantedAt());
  }

  @Test
  void testPlantSeedWithNegativeCoordinates() {
    assertThrows(
        IllegalArgumentException.class,
        () -> plantInstanceService.plantSeed(testGarden.getId(), testPlant.getId(), -1, 5));

    assertThrows(
        IllegalArgumentException.class,
        () -> plantInstanceService.plantSeed(testGarden.getId(), testPlant.getId(), 5, -1));
  }

  @Test
  void testPlantSeedOnOccupiedCell() {
    plantInstanceService.plantSeed(testGarden.getId(), testPlant.getId(), 5, 5);

    assertThrows(
        IllegalStateException.class,
        () -> plantInstanceService.plantSeed(testGarden.getId(), testPlant.getId(), 5, 5));
  }

  @Test
  void testFindPlantInstanceById() {
    PlantInstance created =
        plantInstanceService.plantSeed(testGarden.getId(), testPlant.getId(), 3, 4);

    Optional<PlantInstance> found = plantInstanceService.findPlantInstanceById(created.getId());

    assertTrue(found.isPresent());
    assertEquals(3, found.get().getCellX());
    assertEquals(4, found.get().getCellY());
  }

  @Test
  void testFindPlantInstancesByGardenId() {
    plantInstanceService.plantSeed(testGarden.getId(), testPlant.getId(), 1, 1);
    plantInstanceService.plantSeed(testGarden.getId(), testPlant.getId(), 2, 2);
    plantInstanceService.plantSeed(testGarden.getId(), testPlant.getId(), 3, 3);

    List<PlantInstance> instances =
        plantInstanceService.findPlantInstancesByGardenId(testGarden.getId());

    assertEquals(3, instances.size());
  }

  @Test
  void testFindPlantInstanceAtCell() {
    plantInstanceService.plantSeed(testGarden.getId(), testPlant.getId(), 7, 8);

    Optional<PlantInstance> found =
        plantInstanceService.findPlantInstanceAtCell(testGarden.getId(), 7, 8);

    assertTrue(found.isPresent());
    assertEquals(7, found.get().getCellX());
    assertEquals(8, found.get().getCellY());
  }

  @Test
  void testIsOccupied() {
    assertFalse(plantInstanceService.isOccupied(testGarden.getId(), 5, 5));

    plantInstanceService.plantSeed(testGarden.getId(), testPlant.getId(), 5, 5);

    assertTrue(plantInstanceService.isOccupied(testGarden.getId(), 5, 5));
  }

  @Test
  void testWaterPlant() {
    PlantInstance instance =
        plantInstanceService.plantSeed(testGarden.getId(), testPlant.getId(), 2, 2);
    Integer instanceId = instance.getId();
    assertFalse(instance.getIsWatered());

    plantInstanceService.waterPlant(instanceId);

    Optional<PlantInstance> updated = plantInstanceService.findPlantInstanceById(instanceId);
    assertTrue(updated.isPresent());
    assertTrue(updated.get().getIsWatered());
  }

  @Test
  void testWaterPlantNotFound() {
    assertThrows(IllegalArgumentException.class, () -> plantInstanceService.waterPlant(99999));
  }

  @Test
  void testFertilizePlant() {
    PlantInstance instance =
        plantInstanceService.plantSeed(testGarden.getId(), testPlant.getId(), 3, 3);
    Integer instanceId = instance.getId();
    assertFalse(instance.getIsFertilized());

    plantInstanceService.fertilizePlant(instanceId);

    Optional<PlantInstance> updated = plantInstanceService.findPlantInstanceById(instanceId);
    assertTrue(updated.isPresent());
    assertTrue(updated.get().getIsFertilized());
  }

  @Test
  void testFertilizePlantNotFound() {
    assertThrows(IllegalArgumentException.class, () -> plantInstanceService.fertilizePlant(99999));
  }

  @Test
  void testUpdateGrowthStage() {
    PlantInstance instance =
        plantInstanceService.plantSeed(testGarden.getId(), testPlant.getId(), 4, 4);
    Integer instanceId = instance.getId();
    assertEquals(0, instance.getGrowthStage());

    plantInstanceService.updateGrowthStage(instanceId);

    Optional<PlantInstance> updated = plantInstanceService.findPlantInstanceById(instanceId);
    assertTrue(updated.isPresent());
    assertEquals(0, updated.get().getGrowthStage());
  }

  @Test
  void testIsReadyToHarvest() {
    PlantInstance instance =
        plantInstanceService.plantSeed(testGarden.getId(), testPlant.getId(), 6, 6);

    assertFalse(plantInstanceService.isReadyToHarvest(instance.getId()));

    instance.setGrowthStage(5);
    context.getPlantInstanceRepository().update(instance.getId(), instance);

    assertTrue(plantInstanceService.isReadyToHarvest(instance.getId()));
  }

  @Test
  void testHarvestPlant() {
    PlantInstance instance =
        plantInstanceService.plantSeed(testGarden.getId(), testPlant.getId(), 7, 7);

    instance.setGrowthStage(5);
    context.getPlantInstanceRepository().update(instance.getId(), instance);

    plantInstanceService.harvestPlant(instance.getId());

    Optional<PlantInstance> harvested =
        plantInstanceService.findPlantInstanceById(instance.getId());
    assertFalse(harvested.isPresent());
  }

  @Test
  void testHarvestPlantNotReady() {
    PlantInstance instance =
        plantInstanceService.plantSeed(testGarden.getId(), testPlant.getId(), 8, 8);

    assertThrows(
        IllegalStateException.class, () -> plantInstanceService.harvestPlant(instance.getId()));
  }

  @Test
  void testRemovePlant() {
    PlantInstance instance =
        plantInstanceService.plantSeed(testGarden.getId(), testPlant.getId(), 9, 9);

    plantInstanceService.removePlant(instance.getId());

    Optional<PlantInstance> removed = plantInstanceService.findPlantInstanceById(instance.getId());
    assertFalse(removed.isPresent());
  }
}
