package ua.notion.presentation.game.viewmodel;

import java.util.concurrent.CompletableFuture;
import ua.notion.presentation.game.entity.Player;
import ua.notion.presentation.game.input.KeyboardHandler;
import ua.notion.presentation.game.map.TileMap;

public class GameViewModel {
  private final TileMap tileMap;
  private final Player player;
  private final KeyboardHandler keyboardHandler;

  public GameViewModel(int mapWidth, int mapHeight) {
    this.tileMap = new TileMap(mapWidth, mapHeight);
    this.player = new Player(10, 10);
    this.keyboardHandler = new KeyboardHandler();
  }

  public CompletableFuture<Void> loadAsync() {
    return tileMap.loadAsync();
  }

  public void update(double deltaTime) {
    if (!tileMap.isLoaded()) {
      return;
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
}
