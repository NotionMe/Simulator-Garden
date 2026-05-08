package ua.notion.presentation.game.map;

public class MapObject {
  private final int col;
  private final int row;
  private final String type;

  public MapObject(int col, int row, String type) {
    this.col = col;
    this.row = row;
    this.type = type;
  }

  public int getCol() {
    return col;
  }

  public int getRow() {
    return row;
  }

  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return String.format("MapObject[col=%d, row=%d, type=%s]", col, row, type);
  }
}
