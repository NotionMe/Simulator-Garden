package ua.notion.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import ua.notion.domain.entity.Plant;
import ua.notion.infrastructure.persistence.contract.PlantRepository;

class PlantRepositoryTest extends BaseRepositoryTest {

  @Test
  void shouldSaveAndFindPlantById() {
    PlantRepository repo = context.getPlantRepository();

    Plant plant =
        Plant.builder()
            .name("Tomato")
            .species("Solanum lycopersicum")
            .growthDays(60)
            .climateType("temperate")
            .iconKey("tomato_icon")
            .build();

    Plant saved = repo.save(plant);

    assertNotNull(saved.getId());
    assertEquals("Tomato", saved.getName());
    assertEquals("temperate", saved.getClimateType());

    Optional<Plant> found = repo.findById(saved.getId());
    assertTrue(found.isPresent());
    assertEquals("Solanum lycopersicum", found.get().getSpecies());
  }

  @Test
  void shouldUpdatePlant() {
    PlantRepository repo = context.getPlantRepository();

    Plant plant =
        repo.save(
            Plant.builder()
                .name("Carrot")
                .species("Daucus carota")
                .growthDays(70)
                .climateType("temperate")
                .build());

    plant.setGrowthDays(80);
    plant.setClimateType("tropical");

    repo.update(plant.getId(), plant);

    Optional<Plant> updated = repo.findById(plant.getId());
    assertTrue(updated.isPresent());
    assertEquals(80, updated.get().getGrowthDays());
    assertEquals("tropical", updated.get().getClimateType());
  }

  @Test
  void shouldDeletePlant() {
    PlantRepository repo = context.getPlantRepository();

    Plant plant =
        repo.save(
            Plant.builder()
                .name("ToDelete")
                .species("Test species")
                .growthDays(30)
                .climateType("arid")
                .build());

    Integer id = plant.getId();
    repo.delete(id);

    Optional<Plant> deleted = repo.findById(id);
    assertFalse(deleted.isPresent());
  }

  @Test
  void shouldFindAllPlants() {
    PlantRepository repo = context.getPlantRepository();

    repo.save(
        Plant.builder()
            .name("Plant1")
            .species("Species1")
            .growthDays(30)
            .climateType("temperate")
            .build());
    repo.save(
        Plant.builder()
            .name("Plant2")
            .species("Species2")
            .growthDays(40)
            .climateType("tropical")
            .build());

    List<Plant> plants = repo.findAll();
    assertEquals(2, plants.size());
  }

  @Test
  void shouldEnforceClimateTypeConstraint() {
    PlantRepository repo = context.getPlantRepository();

    assertThrows(
        Exception.class,
        () ->
            repo.save(
                Plant.builder()
                    .name("Invalid")
                    .species("Test")
                    .growthDays(30)
                    .climateType("invalid_climate")
                    .build()));
  }

  @Test
  void shouldEnforceGrowthDaysConstraint() {
    PlantRepository repo = context.getPlantRepository();

    assertThrows(
        Exception.class,
        () ->
            repo.save(
                Plant.builder()
                    .name("Invalid")
                    .species("Test")
                    .growthDays(0)
                    .climateType("temperate")
                    .build()));

    assertThrows(
        Exception.class,
        () ->
            repo.save(
                Plant.builder()
                    .name("Invalid")
                    .species("Test")
                    .growthDays(-5)
                    .climateType("temperate")
                    .build()));
  }

  @Test
  void shouldFindPlantsByClimateType() {
    PlantRepository repo = context.getPlantRepository();

    repo.save(
        Plant.builder()
            .name("Cactus")
            .species("Cactaceae")
            .growthDays(90)
            .climateType("arid")
            .build());
    repo.save(
        Plant.builder()
            .name("Banana")
            .species("Musa")
            .growthDays(120)
            .climateType("tropical")
            .build());
    repo.save(
        Plant.builder()
            .name("Apple")
            .species("Malus")
            .growthDays(180)
            .climateType("temperate")
            .build());

    List<Plant> aridPlants = repo.findByField("climate_type", "arid");
    assertEquals(1, aridPlants.size());
    assertEquals("Cactus", aridPlants.get(0).getName());
  }

  @Test
  void shouldCountPlants() {
    PlantRepository repo = context.getPlantRepository();

    repo.save(
        Plant.builder()
            .name("Plant1")
            .species("Species1")
            .growthDays(30)
            .climateType("temperate")
            .build());
    repo.save(
        Plant.builder()
            .name("Plant2")
            .species("Species2")
            .growthDays(40)
            .climateType("tropical")
            .build());

    long count = repo.count();
    assertEquals(2, count);
  }

  @Test
  void shouldHandleOptionalFields() {
    PlantRepository repo = context.getPlantRepository();

    Plant plant =
        repo.save(
            Plant.builder()
                .name("MinimalPlant")
                .species("Minimal species")
                .growthDays(30)
                .climateType("temperate")
                .build());

    Optional<Plant> found = repo.findById(plant.getId());
    assertTrue(found.isPresent());
    assertNull(found.get().getIconKey());
  }
}
