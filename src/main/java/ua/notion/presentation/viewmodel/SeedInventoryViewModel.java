package ua.notion.presentation.viewmodel;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType;
import ua.notion.presentation.viewmodel.ServerBackedInventory.InventoryEntry;

/** Planting seeds — same server-backed stack as harvested crops. */
public class SeedInventoryViewModel {

  private final ServerBackedInventory inventory;

  public SeedInventoryViewModel(ServerBackedInventory inventory) {
    this.inventory = inventory;
  }

  public void loadFromServer() {
    inventory.loadFromServer();
  }

  public void addSeeds(PlantType plantType, int count) {
    inventory.addItem(plantType, count);
  }

  public boolean hasSeeds(PlantType plantType) {
    return inventory.getCount(plantType) > 0;
  }

  public int getSeedCount(PlantType plantType) {
    return inventory.getCount(plantType);
  }

  public boolean consumeSeed(PlantType plantType) {
    return inventory.removeItem(plantType, 1);
  }

  public List<SeedItem> getAvailableSeeds() {
    List<SeedItem> seeds = new ArrayList<>();
    for (InventoryEntry entry : inventory.getAllEntries()) {
      seeds.add(new SeedItem(entry.getPlantType(), entry.countProperty()));
    }
    return seeds;
  }

  public IntegerProperty getSeedCountProperty(PlantType plantType) {
    return inventory.countProperty(plantType);
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
      return "Growth: " + (plantType.ordinal() + 3) + " days";
    }
  }
}
