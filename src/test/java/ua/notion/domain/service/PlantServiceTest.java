package ua.notion.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.notion.domain.entity.Plant;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.TestDatabaseManager;

class PlantServiceTest {
  private TestDatabaseManager dbManager;
  private PersistenceContext context;
  private PlantService plantService;

  @BeforeEach
  void setUp() {
    dbManager = new TestDatabaseManager();
    context = dbManager.createContext();
    plantService = new PlantService(context);
  }

  @AfterEach
  void tearDown() {
    if (context != null) {
      context.close();
    }
    dbManager.cleanup();
  }

  @Test
  void testCreatePlant() {
    Plant plant =
        plantService.createPlant("Tomato", "Solanum lycopersicum", 60, "tropical", "tomato");

    assertNotNull(plant);
    assertNotNull(plant.getId());
    assertEquals("Tomato", plant.getName());
    assertEquals("Solanum lycopersicum", plant.getSpecies());
    assertEquals(60, plant.getGrowthDays());
    assertEquals("tropical", plant.getClimateType());
    assertEquals("tomato", plant.getIconKey());
  }

  @Test
  void testCreatePlantWithInvalidGrowthDays() {
    assertThrows(
        IllegalArgumentException.class,
        () -> plantService.createPlant("Invalid", "Species", 0, "temperate", "icon"));

    assertThrows(
        IllegalArgumentException.class,
        () -> plantService.createPlant("Invalid", "Species", -5, "temperate", "icon"));
  }

  @Test
  void testFindPlantById() {
    Plant created = plantService.createPlant("Carrot", "Daucus carota", 70, "temperate", "carrot");

    Optional<Plant> found = plantService.findPlantById(created.getId());

    assertTrue(found.isPresent());
    assertEquals("Carrot", found.get().getName());
    assertEquals("Daucus carota", found.get().getSpecies());
  }

  @Test
  void testFindPlantByIdNotFound() {
    Optional<Plant> found = plantService.findPlantById(99999);
    assertFalse(found.isPresent());
  }

  @Test
  void testFindPlantsBySpecies() {
    plantService.createPlant("Tomato1", "Solanum lycopersicum", 60, "tropical", "tomato1");
    plantService.createPlant("Tomato2", "Solanum lycopersicum", 65, "tropical", "tomato2");
    plantService.createPlant("Carrot", "Daucus carota", 70, "temperate", "carrot");

    List<Plant> tomatoes = plantService.findPlantsBySpecies("Solanum lycopersicum");

    assertEquals(2, tomatoes.size());
    assertTrue(tomatoes.stream().allMatch(p -> p.getSpecies().equals("Solanum lycopersicum")));
  }

  @Test
  void testFindPlantsByClimateType() {
    plantService.createPlant("Tomato", "Solanum lycopersicum", 60, "tropical", "tomato");
    plantService.createPlant("Pepper", "Capsicum annuum", 75, "tropical", "pepper");
    plantService.createPlant("Carrot", "Daucus carota", 70, "temperate", "carrot");

    List<Plant> tropicalPlants = plantService.findPlantsByClimateType("tropical");

    assertEquals(2, tropicalPlants.size());
    assertTrue(tropicalPlants.stream().allMatch(p -> p.getClimateType().equals("tropical")));
  }

  @Test
  void testGetAllPlants() {
    plantService.createPlant("Tomato", "Solanum lycopersicum", 60, "tropical", "tomato");
    plantService.createPlant("Carrot", "Daucus carota", 70, "temperate", "carrot");
    plantService.createPlant("Pepper", "Capsicum annuum", 75, "tropical", "pepper");

    List<Plant> allPlants = plantService.getAllPlants();

    assertTrue(allPlants.size() >= 3);
  }

  @Test
  void testUpdatePlant() {
    Plant plant =
        plantService.createPlant("Tomato", "Solanum lycopersicum", 60, "tropical", "tomato");

    plant.setName("Cherry Tomato");
    plant.setGrowthDays(55);
    plantService.updatePlant(plant);

    Optional<Plant> updated = plantService.findPlantById(plant.getId());
    assertTrue(updated.isPresent());
    assertEquals("Cherry Tomato", updated.get().getName());
    assertEquals(55, updated.get().getGrowthDays());
  }

  @Test
  void testUpdatePlantWithNullId() {
    Plant plant = Plant.builder().name("Test").species("Test").growthDays(30).build();

    assertThrows(IllegalArgumentException.class, () -> plantService.updatePlant(plant));
  }

  @Test
  void testUpdatePlantWithInvalidGrowthDays() {
    Plant plant =
        plantService.createPlant("Tomato", "Solanum lycopersicum", 60, "tropical", "tomato");
    plant.setGrowthDays(0);

    assertThrows(IllegalArgumentException.class, () -> plantService.updatePlant(plant));
  }

  @Test
  void testDeletePlant() {
    Plant plant =
        plantService.createPlant("Tomato", "Solanum lycopersicum", 60, "tropical", "tomato");
    Integer plantId = plant.getId();

    plantService.deletePlant(plantId);

    Optional<Plant> deleted = plantService.findPlantById(plantId);
    assertFalse(deleted.isPresent());
  }

  @Test
  void testDeleteNonExistentPlant() {
    assertDoesNotThrow(() -> plantService.deletePlant(99999));
  }
}
