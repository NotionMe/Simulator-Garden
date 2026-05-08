package ua.notion.presentation.game;

import javafx.animation.AnimationTimer;

public class GameLoop extends AnimationTimer {
  private final GameScene scene;
  private long lastUpdate = 0;
  private int frameCount = 0;
  private long lastFpsTime = 0;
  private int fps = 0;

  public GameLoop(GameScene scene) {
    this.scene = scene;
  }

  @Override
  public void handle(long now) {
    if (lastUpdate == 0) {
      lastUpdate = now;
      lastFpsTime = now;
      return;
    }

    double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
    lastUpdate = now;

    updateFps(now);

    scene.update(deltaTime);

    scene.render();
  }

  private void updateFps(long now) {
    frameCount++;
    if (now - lastFpsTime >= 1_000_000_000) {
      fps = frameCount;
      frameCount = 0;
      lastFpsTime = now;
    }
  }

  public int getFps() {
    return fps;
  }
}
