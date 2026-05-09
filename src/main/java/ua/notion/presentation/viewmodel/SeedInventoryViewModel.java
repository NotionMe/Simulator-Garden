package ua.notion.presentation.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType;

public class SeedInventoryViewModel {
  private final Map<PlantType, IntegerProperty> seedInventory;

  public SeedInventoryViewModel() {
    this.seedInventory = new HashMap<>();
    initializeTestInventory();
  }

  private void initializeTestInventory() {
    // Add test seeds for demonstration
    addSeeds(PlantType.TOMATO, 5);
    addSeeds(PlantType.CARROT, 3);
    addSeeds(PlantType.CORN, 7);
    addSeeds(PlantType.BEAN, 2);
    addSeeds(PlantType.SUNFLOWER, 4);
  }

  public void addSeeds(PlantType plantType, int count) {
    seedInventory.computeIfAbsent(plantType, k -> new SimpleIntegerProperty(0));
    seedInventory.get(plantType).set(seedInventory.get(plantType).get() + count);
  }

  public boolean hasSeeds(PlantType plantType) {
    return seedInventory.containsKey(plantType) && seedInventory.get(plantType).get() > 0;
  }

  public int getSeedCount(PlantType plantType) {
    return seedInventory.containsKey(plantType) ? seedInventory.get(plantType).get() : 0;
  }

  public boolean consumeSeed(PlantType plantType) {
    if (!hasSeeds(plantType)) {
      return false;
    }
    int currentCount = seedInventory.get(plantType).get();
    seedInventory.get(plantType).set(currentCount - 1);
    return true;
  }

  public List<SeedItem> getAvailableSeeds() {
    List<SeedItem> seeds = new ArrayList<>();
    for (Map.Entry<PlantType, IntegerProperty> entry : seedInventory.entrySet()) {
      if (entry.getValue().get() > 0) {
        seeds.add(new SeedItem(entry.getKey(), entry.getValue()));
      }
    }
    return seeds;
  }

  public IntegerProperty getSeedCountProperty(PlantType plantType) {
    return seedInventory.computeIfAbsent(plantType, k -> new SimpleIntegerProperty(0));
  }

  public static class SeedItem {
    private final PlantType plantType;
    private final IntegerProperty count;

    public SeedItem(PlantType plantType, IntegerProperty count) {
      this.plantType = plantType;
      this.count = count;
    }

    public PlantType getPlantType() {
      return plantType;
    }

    public IntegerProperty countProperty() {
      return count;
    }

    public int getCount() {
      return count.get();
    }

    public String getDisplayName() {
      return plantType.name().charAt(0) + plantType.name().substring(1).toLowerCase();
    }

    public String getGrowthInfo() {
      // Placeholder - can be enhanced with actual plant data
      return "Growth: " + (plantType.ordinal() + 3) + " days";
    }
  }
}
