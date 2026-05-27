package ua.notion.presentation.game.viewmodel;

import java.util.concurrent.CompletableFuture;
import javafx.scene.input.KeyCode;
import ua.notion.domain.service.PlayerInventoryItemService;
import ua.notion.infrastructure.async.AsyncExecutor;
import ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType;
import ua.notion.presentation.game.catalog.PlantTypeResolver;
import ua.notion.presentation.game.entity.Player;
import ua.notion.presentation.game.input.KeyboardHandler;
import ua.notion.presentation.game.map.TileMap;
import ua.notion.presentation.game.plant.HarvestResult;
import ua.notion.presentation.game.plant.PlantManager;
import ua.notion.presentation.viewmodel.PlayerInventoryViewModel;
import ua.notion.presentation.viewmodel.SeedInventoryViewModel;
import ua.notion.presentation.viewmodel.ServerBackedInventory;

public class GameViewModel {
  private final TileMap tileMap;
  private final Player player;
  private final KeyboardHandler keyboardHandler;
  private final PlantManager plantManager;
  private final SeedInventoryViewModel seedInventory;
  private final PlayerInventoryViewModel playerInventory;

  private String lastHarvestMessage = "";

  public GameViewModel(
      int userId,
      PlayerInventoryItemService inventoryService,
      PlantTypeResolver plantTypes,
      int mapWidth,
      int mapHeight) {
    this.tileMap = new TileMap(mapWidth, mapHeight);
    this.player = new Player(9, 11);
    this.keyboardHandler = new KeyboardHandler();
    this.plantManager = new PlantManager(tileMap.getOrthoCoords());
    ServerBackedInventory sharedInventory =
        new ServerBackedInventory(userId, inventoryService, plantTypes);
    this.seedInventory = new SeedInventoryViewModel(sharedInventory);
    this.playerInventory = new PlayerInventoryViewModel(sharedInventory);
  }

  public CompletableFuture<Void> loadAsync() {
    return tileMap
        .loadAsync()
        .thenCompose(
            ignored ->
                AsyncExecutor.runAsync(
                    () -> {
                      playerInventory.loadFromServer();
                    }));
  }

  public void update(double deltaTime) {
    if (!tileMap.isLoaded()) {
      return;
    }

    plantManager.update(deltaTime);

    if (keyboardHandler.isJustPressed(KeyCode.G)) {
      plantManager.growAllPlants();
    }

    int dx = 0;
    int dy = 0;

    if (keyboardHandler.isMovingUp()) {
      dy -= 1;
    }
    if (keyboardHandler.isMovingDown()) {
      dy += 1;
    }
    if (keyboardHandler.isMovingLeft()) {
      dx -= 1;
    }
    if (keyboardHandler.isMovingRight()) {
      dx += 1;
    }

    if (dx != 0 || dy != 0) {
      player.tryMove(dx, dy, tileMap);
    } else {
      player.stopMoving();
    }

    player.update(deltaTime);
    keyboardHandler.clearJustPressed();
  }

  public HarvestResult tryHarvestAt(int bedAnchorCol, int bedAnchorRow) {
    var plant = plantManager.getPlantAt(bedAnchorCol, bedAnchorRow);
    PlantType cropType = plant != null && plant.isReadyToHarvest() ? plant.getPlantType() : null;

    HarvestResult result = plantManager.tryHarvest(bedAnchorCol, bedAnchorRow, playerInventory);
    lastHarvestMessage =
        switch (result) {
          case SUCCESS -> buildHarvestSuccessMessage(cropType, plantManager.getLastHarvestAmount());
          case NOT_READY -> "Not ripe yet";
          case WITHERED_GONE -> "Crop wilted away";
          case NO_PLANT -> "";
        };
    return result;
  }

  private String buildHarvestSuccessMessage(PlantType type, int amount) {
    if (type == null) {
      return "Added to inventory";
    }
    String name = type.name().charAt(0) + type.name().substring(1).toLowerCase();
    String bonusSuffix = amount > 1 ? " (x" + amount + " Combo!)" : "";
    return "+"
        + amount
        + " "
        + name
        + bonusSuffix
        + " → inventory ("
        + playerInventory.getTotalCount()
        + " total)";
  }

  public String getLastHarvestMessage() {
    return lastHarvestMessage;
  }

  public TileMap getTileMap() {
    return tileMap;
  }

  public Player getPlayer() {
    return player;
  }

  public KeyboardHandler getKeyboardHandler() {
    return keyboardHandler;
  }

  public PlantManager getPlantManager() {
    return plantManager;
  }

  public SeedInventoryViewModel getSeedInventory() {
    return seedInventory;
  }

  public PlayerInventoryViewModel getPlayerInventory() {
    return playerInventory;
  }
}
