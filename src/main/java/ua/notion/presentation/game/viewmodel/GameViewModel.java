package ua.notion.presentation.game.viewmodel;

import java.util.concurrent.CompletableFuture;
import javafx.scene.input.KeyCode;
import ua.notion.presentation.game.entity.Player;
import ua.notion.presentation.game.input.KeyboardHandler;
import ua.notion.presentation.game.map.TileMap;
import ua.notion.presentation.game.plant.PlantManager;

public class GameViewModel {
  private final TileMap tileMap;
  private final Player player;
  private final KeyboardHandler keyboardHandler;
  private final PlantManager plantManager;

  public GameViewModel(int mapWidth, int mapHeight) {
    this.tileMap = new TileMap(mapWidth, mapHeight);
    this.player = new Player(10, 10);
    this.keyboardHandler = new KeyboardHandler();
    this.plantManager = new PlantManager(tileMap.getIsoCoords());
  }

  public CompletableFuture<Void> loadAsync() {
    return tileMap.loadAsync();
  }

  public void update(double deltaTime) {
    if (!tileMap.isLoaded()) {
      return;
    }

    // Handle grow plants key (G)
    if (keyboardHandler.isJustPressed(KeyCode.G)) {
      plantManager.growAllPlants();
      System.out.println("Growing all plants!");
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
}
