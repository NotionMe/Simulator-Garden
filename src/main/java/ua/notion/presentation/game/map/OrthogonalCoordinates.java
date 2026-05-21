package ua.notion.presentation.game.map;

public class OrthogonalCoordinates {

  private final int tileWidth;
  private final int tileHeight;

  public OrthogonalCoordinates(int tileWidth, int tileHeight) {
    this.tileWidth = tileWidth;
    this.tileHeight = tileHeight;
  }

  public double toScreenX(int col, int row) {
    return col * (double) tileWidth;
  }

  public double toScreenY(int col, int row) {
    return row * (double) tileHeight;
  }

  public int toGridCol(double screenX, double screenY) {
    return (int) Math.floor(screenX / tileWidth);
  }

  public int toGridRow(double screenX, double screenY) {
    return (int) Math.floor(screenY / tileHeight);
  }
}
