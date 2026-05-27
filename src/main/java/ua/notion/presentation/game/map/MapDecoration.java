package ua.notion.presentation.game.map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * A decorative sprite object placed on the map. Each decoration references a region in a
 * spritesheet and is rendered at a specific world-space position with optional scaling.
 */
public class MapDecoration {
  private final Image spriteSheet;
  private final int srcX, srcY, srcW, srcH;
  private final double worldX, worldY;
  private final double renderW, renderH;
  private final boolean blocksTile;

  public MapDecoration(
      Image spriteSheet,
      int srcX,
      int srcY,
      int srcW,
      int srcH,
      double worldX,
      double worldY,
      double renderW,
      double renderH,
      boolean blocksTile) {
    this.spriteSheet = spriteSheet;
    this.srcX = srcX;
    this.srcY = srcY;
    this.srcW = srcW;
    this.srcH = srcH;
    this.worldX = worldX;
    this.worldY = worldY;
    this.renderW = renderW;
    this.renderH = renderH;
    this.blocksTile = blocksTile;
  }

  public void render(GraphicsContext gc, double offsetX, double offsetY) {
    gc.drawImage(
        spriteSheet, srcX, srcY, srcW, srcH, worldX + offsetX, worldY + offsetY, renderW, renderH);
  }

  /** Bottom Y edge used for depth-sorting. */
  public double getSortY() {
    return worldY + renderH;
  }

  public double getWorldX() {
    return worldX;
  }

  public double getWorldY() {
    return worldY;
  }

  public double getRenderW() {
    return renderW;
  }

  public double getRenderH() {
    return renderH;
  }

  public boolean blocksTile() {
    return blocksTile;
  }

  /**
   * Returns true if this decoration overlaps a given tile rectangle. Used by the walkability check.
   */
  public boolean occupiesTile(int tileCol, int tileRow, int tilePx) {
    if (!blocksTile) {
      return false;
    }
    double tx = tileCol * tilePx;
    double ty = tileRow * tilePx;
    return tx < worldX + renderW
        && tx + tilePx > worldX
        && ty < worldY + renderH
        && ty + tilePx > worldY;
  }
}
