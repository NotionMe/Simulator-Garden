package ua.notion.presentation.game.assets;

public final class GardenTileAtlas {

  private GardenTileAtlas() {
    throw new AssertionError("Cannot instantiate constants class");
  }

  public static final int TILE_SIZE = 16;
  public static final String SPRING_TILEMAP_PATH = "tilemaps/spring tilemap.png";

  /** Solid grass fill (3,1) — fully opaque. */
  public static final int GRASS_CENTER_COL = 3;

  public static final int GRASS_CENTER_ROW = 1;

  /** Solid 2x2 dirt patch (less transparent than 0,11) — cols 5–6, rows 11–12. */
  public static final int BED_TOP_LEFT_COL = 5;

  public static final int BED_TOP_LEFT_ROW = 11;
  public static final int BED_TOP_RIGHT_COL = 6;
  public static final int BED_TOP_RIGHT_ROW = 11;
  public static final int BED_BOTTOM_LEFT_COL = 5;
  public static final int BED_BOTTOM_LEFT_ROW = 12;
  public static final int BED_BOTTOM_RIGHT_COL = 6;
  public static final int BED_BOTTOM_RIGHT_ROW = 12;

  /** Brown fill for transparent pixels in extracted bed tiles (#b8956a). */
  public static final int BED_FILL_ARGB = 0xFFB8956A;

  public static final int TILE_GRASS = 0;
  public static final int TILE_BED_TOP_LEFT = 1;
  public static final int TILE_BED_TOP_RIGHT = 2;
  public static final int TILE_BED_BOTTOM_LEFT = 3;
  public static final int TILE_BED_BOTTOM_RIGHT = 4;

  public static boolean isGardenBed(int tileId) {
    return tileId >= TILE_BED_TOP_LEFT && tileId <= TILE_BED_BOTTOM_RIGHT;
  }

  /** Top-left tile of the 2x2 bed for planting / sprite anchor. */
  public static int[] bedAnchorForTile(int tileId, int col, int row) {
    return switch (tileId) {
      case TILE_BED_TOP_RIGHT -> new int[] {col - 1, row};
      case TILE_BED_BOTTOM_LEFT -> new int[] {col, row - 1};
      case TILE_BED_BOTTOM_RIGHT -> new int[] {col - 1, row - 1};
      default -> new int[] {col, row};
    };
  }
}
