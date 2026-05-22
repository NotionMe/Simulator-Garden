package ua.notion.presentation.viewmodel;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType;

/** Player inventory: crops successfully harvested in time. */
public class PlayerInventoryViewModel {

  private final Map<PlantType, IntegerProperty> items = new EnumMap<>(PlantType.class);
  private final ReadOnlyIntegerWrapper totalCount = new ReadOnlyIntegerWrapper(0);

  public void addItem(PlantType plantType, int amount) {
    if (amount <= 0) {
      return;
    }
    IntegerProperty count =
        items.computeIfAbsent(
            plantType,
            k -> {
              SimpleIntegerProperty prop = new SimpleIntegerProperty(0);
              prop.addListener((obs, oldVal, newVal) -> recalculateTotal());
              return prop;
            });
    count.set(count.get() + amount);
    recalculateTotal();
  }

  public boolean removeItem(PlantType plantType, int amount) {
    if (amount <= 0 || getCount(plantType) < amount) {
      return false;
    }
    items.get(plantType).set(items.get(plantType).get() - amount);
    recalculateTotal();
    return true;
  }

  public int getCount(PlantType plantType) {
    return items.containsKey(plantType) ? items.get(plantType).get() : 0;
  }

  public IntegerProperty countProperty(PlantType plantType) {
    return items.computeIfAbsent(plantType, k -> new SimpleIntegerProperty(0));
  }

  public int getTotalCount() {
    return totalCount.get();
  }

  public ReadOnlyIntegerWrapper totalCountProperty() {
    return totalCount;
  }

  public boolean isEmpty() {
    return getAllItems().isEmpty();
  }

  public List<InventoryEntry> getAllItems() {
    List<InventoryEntry> list = new ArrayList<>();
    for (Map.Entry<PlantType, IntegerProperty> entry : items.entrySet()) {
      if (entry.getValue().get() > 0) {
        list.add(new InventoryEntry(entry.getKey(), entry.getValue()));
      }
    }
    list.sort((a, b) -> a.getDisplayName().compareToIgnoreCase(b.getDisplayName()));
    return list;
  }

  private void recalculateTotal() {
    int sum = 0;
    for (IntegerProperty prop : items.values()) {
      sum += prop.get();
    }
    totalCount.set(sum);
  }

  public static class InventoryEntry {
    private final PlantType plantType;
    private final IntegerProperty count;

    public InventoryEntry(PlantType plantType, IntegerProperty count) {
      this.plantType = plantType;
      this.count = count;
    }

    public PlantType getPlantType() {
      return plantType;
    }

    public int getCount() {
      return count.get();
    }

    public IntegerProperty countProperty() {
      return count;
    }

    public String getDisplayName() {
      String name = plantType.name().toLowerCase();
      return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
  }
}
