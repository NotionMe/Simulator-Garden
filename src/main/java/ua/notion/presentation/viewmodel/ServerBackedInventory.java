package ua.notion.presentation.viewmodel;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import ua.notion.domain.service.PlayerInventoryItemService;
import ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType;
import ua.notion.presentation.game.catalog.PlantTypeResolver;

/**
 * Inventory backed by {@code playerinventoryitem} on the server (list/create/update via WebSocket).
 */
public class ServerBackedInventory {

  private final PlayerInventoryItemService inventoryService;
  private final PlantTypeResolver plantTypes;
  private final int userId;

  private final Map<PlantType, IntegerProperty> quantities = new EnumMap<>(PlantType.class);
  private final ReadOnlyIntegerWrapper totalCount = new ReadOnlyIntegerWrapper(0);

  public ServerBackedInventory(
      int userId, PlayerInventoryItemService inventoryService, PlantTypeResolver plantTypes) {
    this.userId = userId;
    this.inventoryService = inventoryService;
    this.plantTypes = plantTypes;
  }

  public void loadFromServer() {
    plantTypes.ensureLoaded();
    quantities.clear();
    inventoryService
        .quantitiesByPlantId(userId)
        .forEach(
            (plantId, qty) ->
                plantTypes.fromPlantId(plantId).ifPresent(type -> setLocalCount(type, qty, false)));
    recalculateTotal();
  }

  public void addItem(PlantType plantType, int amount) {
    if (amount <= 0) {
      return;
    }
    int plantId =
        plantTypes
            .toPlantId(plantType)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Plant type "
                            + plantType
                            + " is not registered on the server. Add a matching plant record."));
    inventoryService.addQuantity(userId, plantId, amount);
    int next = getCount(plantType) + amount;
    setLocalCount(plantType, next, false);
    recalculateTotal();
  }

  public boolean removeItem(PlantType plantType, int amount) {
    if (amount <= 0 || getCount(plantType) < amount) {
      return false;
    }
    int plantId =
        plantTypes
            .toPlantId(plantType)
            .orElseThrow(
                () -> new IllegalStateException("Unknown plant type on server: " + plantType));
    if (!inventoryService.removeQuantity(userId, plantId, amount)) {
      return false;
    }
    setLocalCount(plantType, getCount(plantType) - amount, false);
    recalculateTotal();
    return true;
  }

  public int getCount(PlantType plantType) {
    return quantities.containsKey(plantType) ? quantities.get(plantType).get() : 0;
  }

  public IntegerProperty countProperty(PlantType plantType) {
    return quantities.computeIfAbsent(plantType, k -> new SimpleIntegerProperty(0));
  }

  public int getTotalCount() {
    return totalCount.get();
  }

  public ReadOnlyIntegerWrapper totalCountProperty() {
    return totalCount;
  }

  public boolean isEmpty() {
    return getAllEntries().isEmpty();
  }

  public List<InventoryEntry> getAllEntries() {
    List<InventoryEntry> list = new ArrayList<>();
    for (Map.Entry<PlantType, IntegerProperty> entry : quantities.entrySet()) {
      if (entry.getValue().get() > 0) {
        list.add(new InventoryEntry(entry.getKey(), entry.getValue()));
      }
    }
    list.sort((a, b) -> a.getDisplayName().compareToIgnoreCase(b.getDisplayName()));
    return list;
  }

  private void setLocalCount(PlantType plantType, int count, boolean notifyTotal) {
    IntegerProperty prop =
        quantities.computeIfAbsent(
            plantType,
            k -> {
              SimpleIntegerProperty p = new SimpleIntegerProperty(0);
              p.addListener((obs, oldVal, newVal) -> recalculateTotal());
              return p;
            });
    prop.set(count);
    if (notifyTotal) {
      recalculateTotal();
    }
  }

  private void recalculateTotal() {
    int sum = 0;
    for (IntegerProperty prop : quantities.values()) {
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
