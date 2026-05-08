package ua.notion.presentation.game.assets;

public final class GardenTileAtlas {

  private GardenTileAtlas() {
    throw new AssertionError("Cannot instantiate constants class");
  }

  public static final int BLOCK_TILE_WIDTH = 40;
  public static final int BLOCK_TILE_HEIGHT = 34;

  public static final int ATLAS_STEP_X = 35; // Horizontal
  public static final int ATLAS_STEP_Y = 33; // Vertical
  public static final int ATLAS_OFFSET_X = 1; // Left margin
  public static final int ATLAS_OFFSET_Y = 0; // Top margin

  // blocks.png coordinates (216x270 pixels, 6 columns × 8 rows)
  public static final TileCoordinate GRASS_1 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 0 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 0 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);
  public static final TileCoordinate GRASS_2 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 1 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 0 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);
  public static final TileCoordinate GRASS_3 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 2 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 0 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);
  public static final TileCoordinate GRASS_4 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 3 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 0 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);

  // Row 1: Dirt/soil variations
  public static final TileCoordinate DIRT_1 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 0 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 1 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);
  public static final TileCoordinate DIRT_2 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 1 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 1 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);
  public static final TileCoordinate DIRT_3 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 2 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 1 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);
  public static final TileCoordinate DIRT_4 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 3 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 1 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);

  // Row 2: Garden beds
  public static final TileCoordinate GARDEN_BED_1 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 0 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 2 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);
  public static final TileCoordinate GARDEN_BED_2 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 1 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 2 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);
  public static final TileCoordinate GARDEN_BED_3 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 2 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 2 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);
  public static final TileCoordinate GARDEN_BED_4 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 3 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 2 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);

  // Row 3: Water/stone
  public static final TileCoordinate WATER_1 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 0 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 3 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);
  public static final TileCoordinate WATER_2 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 1 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 3 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);
  public static final TileCoordinate STONE_1 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 2 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 3 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);
  public static final TileCoordinate STONE_2 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 3 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 3 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);

  // Row 4: Additional blocks
  public static final TileCoordinate BLOCK_5_1 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 0 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 4 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);
  public static final TileCoordinate BLOCK_5_2 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 1 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 4 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);
  public static final TileCoordinate BLOCK_5_3 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 2 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 4 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);
  public static final TileCoordinate BLOCK_5_4 =
      new TileCoordinate(
          ATLAS_OFFSET_X + 3 * ATLAS_STEP_X,
          ATLAS_OFFSET_Y + 4 * ATLAS_STEP_Y,
          BLOCK_TILE_WIDTH,
          BLOCK_TILE_HEIGHT);

  // Asset file paths
  public static final String BLOCKS_SPRITE_PATH = "garden/blocks.png";
  public static final String MISC_SPRITE_PATH = "garden/misc.png";
}
