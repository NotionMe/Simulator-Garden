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
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.contract.GardenRepository;
import ua.notion.infrastructure.persistence.contract.PlantInstanceRepository;
import ua.notion.infrastructure.persistence.contract.PlantRepository;
import ua.notion.infrastructure.persistence.contract.UserRepository;

class PlantInstanceRepositoryTest extends BaseRepositoryTest {

  @Test
  void shouldSaveAndFindPlantInstanceById() {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();
    PlantRepository plantRepo = context.getPlantRepository();
    PlantInstanceRepository instanceRepo = context.getPlantInstanceRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user")
                .email("user@example.com")
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
                .name("Tomato")
                .species("Solanum lycopersicum")
                .growthDays(60)
                .climateType("temperate")
                .build());

    PlantInstance instance =
        PlantInstance.builder()
            .gardenId(garden.getId())
            .plantId(plant.getId())
            .cellX(5)
            .cellY(5)
            .plantedAt(LocalDate.now())
            .growthStage(0)
            .isWatered(false)
            .isFertilized(false)
            .build();

    PlantInstance saved = instanceRepo.save(instance);

    assertNotNull(saved.getId());
    assertEquals(5, saved.getCellX());
    assertEquals(5, saved.getCellY());

    Optional<PlantInstance> found = instanceRepo.findById(saved.getId());
    assertTrue(found.isPresent());
    assertEquals(0, found.get().getGrowthStage());
  }

  @Test
  void shouldUpdatePlantInstance() {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();
    PlantRepository plantRepo = context.getPlantRepository();
    PlantInstanceRepository instanceRepo = context.getPlantInstanceRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user")
                .email("user@example.com")
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
                .name("Carrot")
                .species("Daucus carota")
                .growthDays(70)
                .climateType("temperate")
                .build());

    PlantInstance instance =
        instanceRepo.save(
            PlantInstance.builder()
                .gardenId(garden.getId())
                .plantId(plant.getId())
                .cellX(3)
                .cellY(3)
                .plantedAt(LocalDate.now())
                .growthStage(0)
                .isWatered(false)
                .isFertilized(false)
                .build());

    instance.setGrowthStage(50);
    instance.setIsWatered(true);
    instance.setIsFertilized(true);

    instanceRepo.update(instance.getId(), instance);

    Optional<PlantInstance> updated = instanceRepo.findById(instance.getId());
    assertTrue(updated.isPresent());
    assertEquals(50, updated.get().getGrowthStage());
    assertTrue(updated.get().getIsWatered());
    assertTrue(updated.get().getIsFertilized());
  }

  @Test
  void shouldDeletePlantInstance() {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();
    PlantRepository plantRepo = context.getPlantRepository();
    PlantInstanceRepository instanceRepo = context.getPlantInstanceRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user")
                .email("user@example.com")
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

    PlantInstance instance =
        instanceRepo.save(
            PlantInstance.builder()
                .gardenId(garden.getId())
                .plantId(plant.getId())
                .cellX(1)
                .cellY(1)
                .plantedAt(LocalDate.now())
                .growthStage(0)
                .isWatered(false)
                .isFertilized(false)
                .build());

    Integer id = instance.getId();
    instanceRepo.delete(id);

    Optional<PlantInstance> deleted = instanceRepo.findById(id);
    assertFalse(deleted.isPresent());
  }

  @Test
  void shouldEnforceUniqueCellConstraint() {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();
    PlantRepository plantRepo = context.getPlantRepository();
    PlantInstanceRepository instanceRepo = context.getPlantInstanceRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user")
                .email("user@example.com")
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

    instanceRepo.save(
        PlantInstance.builder()
            .gardenId(garden.getId())
            .plantId(plant.getId())
            .cellX(2)
            .cellY(2)
            .plantedAt(LocalDate.now())
            .growthStage(0)
            .isWatered(false)
            .isFertilized(false)
            .build());

    assertThrows(
        Exception.class,
        () ->
            instanceRepo.save(
                PlantInstance.builder()
                    .gardenId(garden.getId())
                    .plantId(plant.getId())
                    .cellX(2)
                    .cellY(2)
                    .plantedAt(LocalDate.now())
                    .growthStage(0)
                    .isWatered(false)
                    .isFertilized(false)
                    .build()));
  }

  @Test
  void shouldEnforceGrowthStageConstraint() {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();
    PlantRepository plantRepo = context.getPlantRepository();
    PlantInstanceRepository instanceRepo = context.getPlantInstanceRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user")
                .email("user@example.com")
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

    assertThrows(
        Exception.class,
        () ->
            instanceRepo.save(
                PlantInstance.builder()
                    .gardenId(garden.getId())
                    .plantId(plant.getId())
                    .cellX(1)
                    .cellY(1)
                    .plantedAt(LocalDate.now())
                    .growthStage(101)
                    .isWatered(false)
                    .isFertilized(false)
                    .build()));

    assertThrows(
        Exception.class,
        () ->
            instanceRepo.save(
                PlantInstance.builder()
                    .gardenId(garden.getId())
                    .plantId(plant.getId())
                    .cellX(1)
                    .cellY(1)
                    .plantedAt(LocalDate.now())
                    .growthStage(-1)
                    .isWatered(false)
                    .isFertilized(false)
                    .build()));
  }

  @Test
  void shouldCascadeDeleteWhenGardenDeleted() {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();
    PlantRepository plantRepo = context.getPlantRepository();
    PlantInstanceRepository instanceRepo = context.getPlantInstanceRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user")
                .email("user@example.com")
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

    PlantInstance instance =
        instanceRepo.save(
            PlantInstance.builder()
                .gardenId(garden.getId())
                .plantId(plant.getId())
                .cellX(1)
                .cellY(1)
                .plantedAt(LocalDate.now())
                .growthStage(0)
                .isWatered(false)
                .isFertilized(false)
                .build());

    Integer instanceId = instance.getId();
    gardenRepo.delete(garden.getId());

    Optional<PlantInstance> deleted = instanceRepo.findById(instanceId);
    assertFalse(deleted.isPresent());
  }

  @Test
  void shouldFindInstancesByGardenId() {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();
    PlantRepository plantRepo = context.getPlantRepository();
    PlantInstanceRepository instanceRepo = context.getPlantInstanceRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user")
                .email("user@example.com")
                .createdAt(LocalDateTime.now())
                .build());

    Garden garden1 =
        gardenRepo.save(
            Garden.builder()
                .userId(user.getId())
                .name("Garden1")
                .widthCells(10)
                .heightCells(10)
                .createdAt(LocalDateTime.now())
                .build());

    Garden garden2 =
        gardenRepo.save(
            Garden.builder()
                .userId(user.getId())
                .name("Garden2")
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

    instanceRepo.save(
        PlantInstance.builder()
            .gardenId(garden1.getId())
            .plantId(plant.getId())
            .cellX(1)
            .cellY(1)
            .plantedAt(LocalDate.now())
            .growthStage(0)
            .isWatered(false)
            .isFertilized(false)
            .build());

    instanceRepo.save(
        PlantInstance.builder()
            .gardenId(garden1.getId())
            .plantId(plant.getId())
            .cellX(2)
            .cellY(2)
            .plantedAt(LocalDate.now())
            .growthStage(0)
            .isWatered(false)
            .isFertilized(false)
            .build());

    instanceRepo.save(
        PlantInstance.builder()
            .gardenId(garden2.getId())
            .plantId(plant.getId())
            .cellX(1)
            .cellY(1)
            .plantedAt(LocalDate.now())
            .growthStage(0)
            .isWatered(false)
            .isFertilized(false)
            .build());

    List<PlantInstance> garden1Instances = instanceRepo.findByField("garden_id", garden1.getId());
    assertEquals(2, garden1Instances.size());
  }

  @Test
  void shouldCountPlantInstances() {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();
    PlantRepository plantRepo = context.getPlantRepository();
    PlantInstanceRepository instanceRepo = context.getPlantInstanceRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user")
                .email("user@example.com")
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

    instanceRepo.save(
        PlantInstance.builder()
            .gardenId(garden.getId())
            .plantId(plant.getId())
            .cellX(1)
            .cellY(1)
            .plantedAt(LocalDate.now())
            .growthStage(0)
            .isWatered(false)
            .isFertilized(false)
            .build());

    instanceRepo.save(
        PlantInstance.builder()
            .gardenId(garden.getId())
            .plantId(plant.getId())
            .cellX(2)
            .cellY(2)
            .plantedAt(LocalDate.now())
            .growthStage(0)
            .isWatered(false)
            .isFertilized(false)
            .build());

    long count = instanceRepo.count();
    assertEquals(2, count);
  }
}
