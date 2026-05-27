package ua.notion.presentation.game.assets;

public final class GardenTileAtlas {

  private GardenTileAtlas() {
    throw new AssertionError("Cannot instantiate constants class");
  }

  public static final int TILE_SIZE = 16;
  public static final String SPRING_TILEMAP_PATH = "tilemaps/spring tilemap.png";

  /** Solid grass fill (3,1). */
  public static final int GRASS_CENTER_COL = 3;

  public static final int GRASS_CENTER_ROW = 1;
  public static final int GRASS_FILL_ARGB = 0xFF5A9E4A;

  /** Grass tuft (0,1). */
  public static final int GRASS_VAR_COL = 0;

  public static final int GRASS_VAR_ROW = 1;

  /** Walkway — flat tan interior (2,12). */
  public static final int PATH_CENTER_COL = 2;

  public static final int PATH_CENTER_ROW = 12;
  public static final int PATH_FILL_ARGB = 0xFFC9A567;

  /** Tilled bed — darker soil patch (5–6, 11–12), distinct from paths. */
  public static final int BED_TOP_LEFT_COL = 5;

  public static final int BED_TOP_LEFT_ROW = 11;
  public static final int BED_TOP_RIGHT_COL = 6;
  public static final int BED_TOP_RIGHT_ROW = 11;
  public static final int BED_BOTTOM_LEFT_COL = 5;
  public static final int BED_BOTTOM_LEFT_ROW = 12;
  public static final int BED_BOTTOM_RIGHT_COL = 6;
  public static final int BED_BOTTOM_RIGHT_ROW = 12;
  public static final int BED_FILL_ARGB = 0xFFB8956A;

  /** Water center (2,7). */
  public static final int WATER_CENTER_COL = 2;

  public static final int WATER_CENTER_ROW = 7;
  public static final int WATER_FILL_ARGB = 0xFF5B9BD5;

  // ── Tile IDs ──────────────────────────────────────────────────────────

  public static final int TILE_GRASS = 0;
  public static final int TILE_BED_TOP_LEFT = 1;
  public static final int TILE_BED_TOP_RIGHT = 2;
  public static final int TILE_BED_BOTTOM_LEFT = 3;
  public static final int TILE_BED_BOTTOM_RIGHT = 4;
  public static final int TILE_DIRT = 5;
  public static final int TILE_WATER = 6;
  public static final int TILE_GRASS_VAR = 7;

  public static final int TILE_COUNT = 8;

  public static boolean isGardenBed(int tileId) {
    return tileId >= TILE_BED_TOP_LEFT && tileId <= TILE_BED_BOTTOM_RIGHT;
  }

  public static int[] bedAnchorForTile(int tileId, int col, int row) {
    return switch (tileId) {
      case TILE_BED_TOP_RIGHT -> new int[] {col - 1, row};
      case TILE_BED_BOTTOM_LEFT -> new int[] {col, row - 1};
      case TILE_BED_BOTTOM_RIGHT -> new int[] {col - 1, row - 1};
      default -> new int[] {col, row};
    };
  }
}
