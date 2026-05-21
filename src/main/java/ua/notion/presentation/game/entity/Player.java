package ua.notion.presentation.game.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import ua.notion.presentation.game.map.TileMap;

public class Player {
  private static final double SPEED = 120.0;
  private static final int SPRITE_WIDTH = 24;
  private static final int SPRITE_HEIGHT = 24;
  private static final int FRAME_COLS = 4;

  private double x, y;
  private int currentTileX, currentTileY;
  private int targetTileX, targetTileY;
  private Image spriteSheet;
  private int animationFrame;
  private double animationTime;
  private int facingRow = 0; // 0: down, 1: up, 2: side
  private boolean flipHorizontal = false;
  private boolean isMoving;

  public Player(int startTileX, int startTileY) {
    this.currentTileX = startTileX;
    this.currentTileY = startTileY;
    this.targetTileX = startTileX;
    this.targetTileY = startTileY;
    this.x = startTileX * 48.0;
    this.y = startTileY * 48.0;
    this.animationFrame = 0;
    this.animationTime = 0;
    this.isMoving = false;
    loadSprites();
  }

  private void loadSprites() {
    spriteSheet =
        new Image(
            getClass()
                .getResourceAsStream(
                    "/assets/sprites/characters/main character/walk and idle.png"));
  }

  public void tryMove(int dx, int dy, TileMap tileMap) {
    if (isMoving) {
      return;
    }

    int newTileX = currentTileX + dx;
    int newTileY = currentTileY + dy;

    // Update facing direction based on movement input
    if (dy > 0) {
      facingRow = 0;
      flipHorizontal = false;
    } else if (dy < 0) {
      facingRow = 1;
      flipHorizontal = false;
    } else if (dx > 0) {
      facingRow = 2;
      flipHorizontal = false;
    } else if (dx < 0) {
      facingRow = 2;
      flipHorizontal = true;
    }

    if (tileMap.isWalkable(newTileX, newTileY)) {
      targetTileX = newTileX;
      targetTileY = newTileY;
      isMoving = true;
    }
  }

  public void update(double deltaTime) {
    if (isMoving) {
      double targetX = targetTileX * 48.0;
      double targetY = targetTileY * 48.0;

      double dx = targetX - x;
      double dy = targetY - y;
      double distance = Math.sqrt(dx * dx + dy * dy);

      if (distance < SPEED * deltaTime) {
        x = targetX;
        y = targetY;
        currentTileX = targetTileX;
        currentTileY = targetTileY;
        isMoving = false;
      } else {
        x += (dx / distance) * SPEED * deltaTime;
        y += (dy / distance) * SPEED * deltaTime;
      }

      animationTime += deltaTime;
      if (animationTime > 0.12) {
        animationFrame = (animationFrame + 1) % FRAME_COLS;
        animationTime = 0;
      }
    } else {
      animationFrame = 0;
    }
  }

  public void stopMoving() {
    isMoving = false;
  }

  public void render(GraphicsContext gc, double offsetX, double offsetY) {
    if (spriteSheet == null) {
      return;
    }

    // Idle columns: 0..3, Walk columns: 4..7 (Note: Row 0 (down) has empty cols 4-7, so we reuse
    // cols 0-3)
    int frameCol = animationFrame;
    if (isMoving && facingRow != 0) {
      frameCol += 4;
    }
    int frameX = frameCol * SPRITE_WIDTH;
    int frameY = facingRow * SPRITE_HEIGHT;

    double renderWidth = 48.0;
    double renderHeight = 48.0;

    gc.save();
    if (flipHorizontal) {
      gc.translate(x + offsetX + 24.0, 0);
      gc.scale(-1, 1);
      gc.translate(-(x + offsetX + 24.0), 0);
    }

    gc.drawImage(
        spriteSheet,
        frameX,
        frameY,
        SPRITE_WIDTH,
        SPRITE_HEIGHT,
        x + offsetX + 24.0 - renderWidth / 2.0,
        y + offsetY + 48.0 - renderHeight,
        renderWidth,
        renderHeight);

    gc.restore();
  }

  public int getCurrentTileX() {
    return currentTileX;
  }

  public int getCurrentTileY() {
    return currentTileY;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public boolean isMoving() {
    return isMoving;
  }
}
