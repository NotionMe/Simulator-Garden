package ua.notion.presentation.game.map;

/** Tile type enumeration for the game map Defines all possible tile types and their properties */
public enum TileType {
  GRASS(true, "Grass"),
  DIRT(true, "Dirt path"),
  GARDEN_BED(false, "Garden bed"),
  HOUSE_WALL(false, "House wall"),
  HOUSE_DOOR(true, "House door"),
  WATER(false, "Water"),
  STONE(false, "Stone");

  private final boolean walkable;
  private final String displayName;

  TileType(boolean walkable, String displayName) {
    this.walkable = walkable;
    this.displayName = displayName;
  }

  public boolean isWalkable() {
    return walkable;
  }

  public String getDisplayName() {
    return displayName;
  }
}
