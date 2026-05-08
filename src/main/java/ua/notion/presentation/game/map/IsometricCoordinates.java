package ua.notion.presentation.game.map;

public class IsometricCoordinates {

  private final int tileWidth;
  private final int tileHeight;

  public IsometricCoordinates(int tileWidth, int tileHeight) {
    this.tileWidth = tileWidth;
    this.tileHeight = tileHeight;
  }

  public double toScreenX(int col, int row) {
    return (col - row) * 20.0;
  }

  public double toScreenY(int col, int row) {
    return (col + row) * 10.0;
  }

  public int toGridCol(double screenX, double screenY) {
    double col = (screenX / 20.0 + screenY / 10.0) / 2.0;
    return (int) Math.round(col);
  }

  public int toGridRow(double screenX, double screenY) {
    double row = (screenY / 10.0 - screenX / 20.0) / 2.0;
    return (int) Math.round(row);
  }
}
