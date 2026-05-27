package ua.notion.presentation.viewmodel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ua.notion.domain.service.PlayerInventoryItemService;
import ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType;
import ua.notion.presentation.game.catalog.PlantTypeResolver;

public class ShopViewModel {

  private static final int STARTING_COINS = 120;

  private final PlayerInventoryItemService inventoryService;
  private final PlantTypeResolver plantTypes;
  private final ObservableList<ShopItem> items = FXCollections.observableArrayList();
  private final IntegerProperty coins = new SimpleIntegerProperty(STARTING_COINS);
  private final StringProperty statusMessage = new SimpleStringProperty("");

  private PlayerInventoryViewModel inventory;
  private int currentUserId;

  public ShopViewModel(PlayerInventoryItemService inventoryService, PlantTypeResolver plantTypes) {
    this.inventoryService = inventoryService;
    this.plantTypes = plantTypes;
  }

  public void setCurrentUserId(int userId) {
    this.currentUserId = userId;
    ServerBackedInventory sharedInventory =
        new ServerBackedInventory(userId, inventoryService, plantTypes);
    this.inventory = new PlayerInventoryViewModel(sharedInventory);
    load();
  }

  public void load() {
    if (currentUserId <= 0 || inventory == null) {
      return;
    }
    inventory.loadFromServer();
    items.setAll(
        plantTypes.registeredPlantTypes().stream()
            .map(type -> new ShopItem(type, inventory.countProperty(type)))
            .toList());
  }

  public boolean buy(ShopItem item) {
    if (item == null) {
      return false;
    }
    if (coins.get() < item.getBuyPrice()) {
      statusMessage.set("Not enough coins");
      return false;
    }
    inventory.addItem(item.getPlantType(), 1);
    coins.set(coins.get() - item.getBuyPrice());
    statusMessage.set("Bought " + item.getDisplayName());
    return true;
  }

  public boolean sell(ShopItem item) {
    if (item == null) {
      return false;
    }
    if (!inventory.removeItem(item.getPlantType(), 1)) {
      statusMessage.set("Nothing to sell");
      return false;
    }
    coins.set(coins.get() + item.getSellPrice());
    statusMessage.set("Sold " + item.getDisplayName());
    return true;
  }

  public ObservableList<ShopItem> getItems() {
    return items;
  }

  public IntegerProperty coinsProperty() {
    return coins;
  }

  public StringProperty statusMessageProperty() {
    return statusMessage;
  }

  public void setStatusMessage(String message) {
    statusMessage.set(message);
  }

  public static class ShopItem {
    private final PlantType plantType;
    private final IntegerProperty ownedCount;
    private final int buyPrice;
    private final int sellPrice;

    public ShopItem(PlantType plantType, IntegerProperty ownedCount) {
      this.plantType = plantType;
      this.ownedCount = ownedCount;
      this.buyPrice = 12 + plantType.ordinal() * 4;
      this.sellPrice = Math.max(4, buyPrice / 2);
    }

    public PlantType getPlantType() {
      return plantType;
    }

    public String getDisplayName() {
      String value = plantType.name().toLowerCase();
      return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    public int getOwnedCount() {
      return ownedCount.get();
    }

    public IntegerProperty ownedCountProperty() {
      return ownedCount;
    }

    public int getBuyPrice() {
      return buyPrice;
    }

    public int getSellPrice() {
      return sellPrice;
    }
  }
}
