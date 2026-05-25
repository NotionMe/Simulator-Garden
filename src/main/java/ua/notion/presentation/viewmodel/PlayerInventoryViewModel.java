package ua.notion.presentation.viewmodel;

import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType;
import ua.notion.presentation.viewmodel.ServerBackedInventory.InventoryEntry;

/** Harvested crops inventory persisted on the server. */
public class PlayerInventoryViewModel {

  private final ServerBackedInventory inventory;

  public PlayerInventoryViewModel(ServerBackedInventory inventory) {
    this.inventory = inventory;
  }

  public void loadFromServer() {
    inventory.loadFromServer();
  }

  public void addItem(PlantType plantType, int amount) {
    inventory.addItem(plantType, amount);
  }

  public boolean removeItem(PlantType plantType, int amount) {
    return inventory.removeItem(plantType, amount);
  }

  public int getCount(PlantType plantType) {
    return inventory.getCount(plantType);
  }

  public IntegerProperty countProperty(PlantType plantType) {
    return inventory.countProperty(plantType);
  }

  public int getTotalCount() {
    return inventory.getTotalCount();
  }

  public ReadOnlyIntegerWrapper totalCountProperty() {
    return inventory.totalCountProperty();
  }

  public boolean isEmpty() {
    return inventory.isEmpty();
  }

  public List<InventoryEntry> getAllItems() {
    return inventory.getAllEntries();
  }
}
