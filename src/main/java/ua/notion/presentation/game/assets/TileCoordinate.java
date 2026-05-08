package ua.notion.presentation.game.assets;

public class TileCoordinate {
  private final int x;
  private final int y;
  private final int width;
  private final int height;

  public TileCoordinate(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  @Override
  public String toString() {
    return String.format("TileCoordinate[x=%d, y=%d, w=%d, h=%d]", x, y, width, height);
  }
}
