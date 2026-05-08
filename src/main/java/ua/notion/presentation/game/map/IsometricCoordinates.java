package ua.notion.presentation.game.map;

public class IsometricCoordinates {

  private final int tileWidth;
  private final int tileHeight;

  public IsometricCoordinates(int tileWidth, int tileHeight) {
    this.tileWidth = tileWidth;
    this.tileHeight = tileHeight;
  }

  public double toScreenX(int col, int row) {
    return (col - row) * 20.0; // 20 * 1.5 scale
  }

  public double toScreenY(int col, int row) {
    return (col + row) * 10.0; // 10 * 1.5 scale
  }

  public int toGridCol(double screenX, double screenY) {
    double col = (screenX / (tileWidth / 2.0) + screenY / (tileHeight / 4.0)) / 2.0;
    return (int) Math.round(col);
  }

  public int toGridRow(double screenX, double screenY) {
    double row = (screenY / (tileHeight / 4.0) - screenX / (tileWidth / 2.0)) / 2.0;
    return (int) Math.round(row);
  }
}
