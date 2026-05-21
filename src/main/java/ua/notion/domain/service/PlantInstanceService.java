package ua.notion.domain.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import ua.notion.domain.entity.Plant;
import ua.notion.domain.entity.PlantInstance;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.contract.PlantInstanceRepository;
import ua.notion.infrastructure.persistence.contract.PlantRepository;

public class PlantInstanceService {
  private final PersistenceContext context;
  private final PlantInstanceRepository plantInstanceRepository;
  private final PlantRepository plantRepository;

  public PlantInstanceService(PersistenceContext context) {
    this.context = context;
    this.plantInstanceRepository = context.getPlantInstanceRepository();
    this.plantRepository = context.getPlantRepository();
  }

  public PlantInstance plantSeed(Integer gardenId, Integer plantId, Integer cellX, Integer cellY) {
    if (cellX < 0 || cellY < 0) {
      throw new IllegalArgumentException("Cell coordinates must be non-negative");
    }

    if (isOccupied(gardenId, cellX, cellY)) {
      throw new IllegalStateException("Cell is already occupied");
    }

    PlantInstance instance =
        PlantInstance.builder()
            .gardenId(gardenId)
            .plantId(plantId)
            .cellX(cellX)
            .cellY(cellY)
            .plantedAt(LocalDate.now())
            .growthStage(0)
            .isWatered(false)
            .isFertilized(false)
            .build();

    try {
      return plantInstanceRepository.save(instance);
    } catch (Exception e) {
      throw new RuntimeException("Failed to plant seed", e);
    }
  }

  public Optional<PlantInstance> findPlantInstanceById(Integer id) {
    return plantInstanceRepository.findById(id);
  }

  public List<PlantInstance> findPlantInstancesByGardenId(Integer gardenId) {
    return plantInstanceRepository.findAll().stream()
        .filter(pi -> pi.getGardenId().equals(gardenId))
        .toList();
  }

  public Optional<PlantInstance> findPlantInstanceAtCell(
      Integer gardenId, Integer cellX, Integer cellY) {
    return plantInstanceRepository.findAll().stream()
        .filter(
            pi ->
                pi.getGardenId().equals(gardenId)
                    && pi.getCellX().equals(cellX)
                    && pi.getCellY().equals(cellY))
        .findFirst();
  }

  public boolean isOccupied(Integer gardenId, Integer cellX, Integer cellY) {
    return findPlantInstanceAtCell(gardenId, cellX, cellY).isPresent();
  }

  public void waterPlant(Integer plantInstanceId) {
    Optional<PlantInstance> instanceOpt = plantInstanceRepository.findById(plantInstanceId);
    if (instanceOpt.isEmpty()) {
      throw new IllegalArgumentException("Plant instance not found");
    }

    PlantInstance instance = instanceOpt.get();
    instance.setIsWatered(true);

    try {
      plantInstanceRepository.update(instance.getId(), instance);
    } catch (Exception e) {
      throw new RuntimeException("Failed to water plant", e);
    }
  }

  public void fertilizePlant(Integer plantInstanceId) {
    Optional<PlantInstance> instanceOpt = plantInstanceRepository.findById(plantInstanceId);
    if (instanceOpt.isEmpty()) {
      throw new IllegalArgumentException("Plant instance not found");
    }

    PlantInstance instance = instanceOpt.get();
    instance.setIsFertilized(true);

    try {
      plantInstanceRepository.update(instance.getId(), instance);
    } catch (Exception e) {
      throw new RuntimeException("Failed to fertilize plant", e);
    }
  }

  public void updateGrowthStage(Integer plantInstanceId) {
    Optional<PlantInstance> instanceOpt = plantInstanceRepository.findById(plantInstanceId);
    if (instanceOpt.isEmpty()) {
      throw new IllegalArgumentException("Plant instance not found");
    }

    PlantInstance instance = instanceOpt.get();
    Optional<Plant> plantOpt = plantRepository.findById(instance.getPlantId());
    if (plantOpt.isEmpty()) {
      throw new IllegalArgumentException("Plant not found");
    }

    Plant plant = plantOpt.get();
    long daysSincePlanted = ChronoUnit.DAYS.between(instance.getPlantedAt(), LocalDate.now());
    int maxStage = 5;
    int newStage = (int) Math.min(daysSincePlanted * maxStage / plant.getGrowthDays(), maxStage);

    if (newStage != instance.getGrowthStage()) {
      instance.setGrowthStage(newStage);

      try {
        plantInstanceRepository.update(instance.getId(), instance);
      } catch (Exception e) {
        throw new RuntimeException("Failed to update growth stage", e);
      }
    }
  }

  public boolean isReadyToHarvest(Integer plantInstanceId) {
    Optional<PlantInstance> instanceOpt = plantInstanceRepository.findById(plantInstanceId);
    if (instanceOpt.isEmpty()) {
      return false;
    }

    PlantInstance instance = instanceOpt.get();
    return instance.getGrowthStage() >= 5;
  }

  public void harvestPlant(Integer plantInstanceId) {
    if (!isReadyToHarvest(plantInstanceId)) {
      throw new IllegalStateException("Plant is not ready to harvest");
    }

    try {
      plantInstanceRepository.delete(plantInstanceId);
    } catch (Exception e) {
      throw new RuntimeException("Failed to harvest plant", e);
    }
  }

  public void removePlant(Integer plantInstanceId) {
    try {
      plantInstanceRepository.delete(plantInstanceId);
    } catch (Exception e) {
      throw new RuntimeException("Failed to remove plant", e);
    }
  }
}
