package ua.notion.presentation.game.catalog;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ua.notion.domain.entity.Plant;
import ua.notion.domain.service.PlantService;
import ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType;

/** Maps server {@link Plant} records to in-game {@link PlantType} sprites. */
public class PlantTypeResolver {

  private final PlantService plantService;
  private final Map<Integer, PlantType> byPlantId = new HashMap<>();
  private final Map<PlantType, Integer> byPlantType = new EnumMap<>(PlantType.class);
  private volatile boolean loaded;

  public PlantTypeResolver(PlantService plantService) {
    this.plantService = plantService;
  }

  public synchronized void ensureLoaded() {
    if (loaded) {
      return;
    }
    for (Plant plant : plantService.getAllPlants()) {
      resolvePlantType(plant).ifPresent(type -> register(plant.getId(), type));
    }
    loaded = true;
  }

  public Optional<PlantType> fromPlantId(Integer plantId) {
    ensureLoaded();
    return Optional.ofNullable(byPlantId.get(plantId));
  }

  public Optional<Integer> toPlantId(PlantType plantType) {
    ensureLoaded();
    return Optional.ofNullable(byPlantType.get(plantType));
  }

  public int requirePlantId(PlantType plantType) {
    return toPlantId(plantType)
        .orElseThrow(() -> new IllegalStateException("No server plant for type: " + plantType));
  }

  public List<PlantType> registeredPlantTypes() {
    ensureLoaded();
    return byPlantType.keySet().stream().sorted().toList();
  }

  private void register(Integer plantId, PlantType type) {
    byPlantId.put(plantId, type);
    byPlantType.putIfAbsent(type, plantId);
  }

  private static Optional<PlantType> resolvePlantType(Plant plant) {
    if (plant.getIconKey() != null && !plant.getIconKey().isBlank()) {
      String key = plant.getIconKey().toLowerCase().replace("_icon", "").replace("-", "_");
      Optional<PlantType> fromKey = tryValueOf(key);
      if (fromKey.isPresent()) {
        return fromKey;
      }
    }
    if (plant.getName() != null && !plant.getName().isBlank()) {
      String key = plant.getName().toLowerCase().replace(" ", "_");
      return tryValueOf(key);
    }
    return Optional.empty();
  }

  private static Optional<PlantType> tryValueOf(String key) {
    try {
      return Optional.of(PlantType.valueOf(key.toUpperCase()));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
