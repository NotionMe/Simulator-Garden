package ua.notion.domain.service;

import java.util.List;
import java.util.Optional;
import ua.notion.domain.entity.Plant;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.contract.PlantRepository;

public class PlantService {
  private final PersistenceContext context;
  private final PlantRepository plantRepository;

  public PlantService(PersistenceContext context) {
    this.context = context;
    this.plantRepository = context.getPlantRepository();
  }

  public Plant createPlant(
      String name, String species, Integer growthDays, String climateType, String iconKey) {
    if (growthDays <= 0) {
      throw new IllegalArgumentException("Growth days must be positive");
    }

    Plant plant =
        Plant.builder()
            .name(name)
            .species(species)
            .growthDays(growthDays)
            .climateType(climateType)
            .iconKey(iconKey)
            .build();

    try {
      return plantRepository.save(plant);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create plant", e);
    }
  }

  public Optional<Plant> findPlantById(Integer id) {
    return plantRepository.findById(id);
  }

  public List<Plant> findPlantsBySpecies(String species) {
    return plantRepository.findAll().stream()
        .filter(p -> p.getSpecies().equalsIgnoreCase(species))
        .toList();
  }

  public List<Plant> findPlantsByClimateType(String climateType) {
    return plantRepository.findAll().stream()
        .filter(p -> p.getClimateType().equalsIgnoreCase(climateType))
        .toList();
  }

  public List<Plant> getAllPlants() {
    return plantRepository.findAll();
  }

  public void updatePlant(Plant plant) {
    if (plant.getId() == null) {
      throw new IllegalArgumentException("Plant ID cannot be null");
    }
    if (plant.getGrowthDays() <= 0) {
      throw new IllegalArgumentException("Growth days must be positive");
    }

    try {
      plantRepository.update(plant.getId(), plant);
    } catch (Exception e) {
      throw new RuntimeException("Failed to update plant", e);
    }
  }

  public void deletePlant(Integer id) {
    try {
      plantRepository.delete(id);
    } catch (Exception e) {
      throw new RuntimeException("Failed to delete plant", e);
    }
  }
}
