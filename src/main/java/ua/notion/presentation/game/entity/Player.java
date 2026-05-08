package ua.notion.presentation.game.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import ua.notion.presentation.game.map.TileMap;

public class Player {
  private static final double SPEED = 80.0;
  private static final int SPRITE_WIDTH = 130;
  private static final int SPRITE_HEIGHT = 160;
  private static final int FRAME_COLS = 4;
  private static final int IDLE_ROW = 0;
  private static final int RUN_ROW = 4;

  private double x, y;
  private int currentTileX, currentTileY;
  private int targetTileX, targetTileY;
  private Image frontSpriteSheet;
  private Image rearSpriteSheet;
  private int animationFrame;
  private double animationTime;
  private boolean movingUp;
  private boolean isMoving;

  public Player(int startTileX, int startTileY) {
    this.currentTileX = startTileX;
    this.currentTileY = startTileY;
    this.targetTileX = startTileX;
    this.targetTileY = startTileY;
    this.x = (startTileX - startTileY) * 20.0;
    this.y = (startTileX + startTileY) * 10.0;
    this.animationFrame = 0;
    this.animationTime = 0;
    this.movingUp = false;
    this.isMoving = false;
    loadSprites();
  }

  private void loadSprites() {
    frontSpriteSheet =
        new Image(
            getClass()
                .getResourceAsStream(
                    "/assets/sprites/isometric_character_template_2023_06_16/template-spritesheet-front 2.png"));
    rearSpriteSheet =
        new Image(
            getClass()
                .getResourceAsStream(
                    "/assets/sprites/isometric_character_template_2023_06_16/template-spritesheet-rear 2.png"));
  }

  public void tryMove(int dx, int dy, TileMap tileMap) {
    if (isMoving) {
      return;
    }

    int newTileX = currentTileX + dx;
    int newTileY = currentTileY + dy;

    if (tileMap.isWalkable(newTileX, newTileY)) {
      targetTileX = newTileX;
      targetTileY = newTileY;
      isMoving = true;
      movingUp = (dy < 0);
    }
  }

  public void update(double deltaTime) {
    if (isMoving) {
      double targetX = (targetTileX - targetTileY) * 20.0;
      double targetY = (targetTileX + targetTileY) * 10.0;

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
    Image currentSheet = movingUp ? rearSpriteSheet : frontSpriteSheet;

    int frameX = animationFrame * SPRITE_WIDTH;
    int frameY = (isMoving ? RUN_ROW : IDLE_ROW) * SPRITE_HEIGHT;

    double renderWidth = SPRITE_WIDTH * 0.35;
    double renderHeight = SPRITE_HEIGHT * 0.35;

    gc.drawImage(
        currentSheet,
        frameX,
        frameY,
        SPRITE_WIDTH,
        SPRITE_HEIGHT,
        x + offsetX - renderWidth / 2.0,
        y + offsetY - renderHeight + 10,
        renderWidth,
        renderHeight);
  }

  public int getCurrentTileX() {
    return currentTileX;
  }

  public int getCurrentTileY() {
    return currentTileY;
  }

  public boolean isMoving() {
    return isMoving;
  }
}
