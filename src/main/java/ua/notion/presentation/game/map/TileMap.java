package ua.notion.presentation.game.map;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import ua.notion.infrastructure.async.AsyncExecutor;
import ua.notion.presentation.game.assets.GardenTileAtlas;
import ua.notion.presentation.game.assets.ObjectSpriteAtlas;
import ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType;
import ua.notion.presentation.game.plant.PlantManager;
import ua.notion.presentation.game.util.ResourceLoader;

public class TileMap {
  private final int width;
  private final int height;
  private final int[][] tiles;

  private Image[] tileImages;
  private OrthogonalCoordinates orthoCoords;
  private boolean isLoaded = false;

  /** Ground-level decorations rendered right after tiles (flowers, stones). */
  private final List<MapDecoration> groundDecorations = new ArrayList<>();

  /** Tall decorations rendered depth-sorted with entities (trees, house). */
  private final List<MapDecoration> tallDecorations = new ArrayList<>();

  /** Loaded decoration spritesheets. */
  private Image itemsSheet;

  private Image miscSheet;
  private MapDecoration shopBench;

  private static final int TILE_PX = 48;

  public TileMap(int width, int height) {
    this.width = width;
    this.height = height;
    this.tiles = new int[height][width];
    this.orthoCoords = new OrthogonalCoordinates(TILE_PX, TILE_PX);
  }

  public CompletableFuture<Void> loadAsync() {
    return AsyncExecutor.runAsync(
            () -> {
              loadTiles();
              loadDecorationSheets();
              generateFarmMap();
            })
        .thenRun(
            () -> {
              isLoaded = true;
              System.out.println("TileMap loaded successfully");
            });
  }

  // ── Tile loading ──────────────────────────────────────────────────────

  private void loadTiles() {
    System.out.println("Loading garden tiles...");
    Image tilemapSheet = ResourceLoader.loadImage(GardenTileAtlas.SPRING_TILEMAP_PATH);

    tileImages = new Image[GardenTileAtlas.TILE_COUNT];
    int ts = GardenTileAtlas.TILE_SIZE;

    tileImages[GardenTileAtlas.TILE_GRASS] =
        ResourceLoader.extractSolidTile(
            tilemapSheet,
            GardenTileAtlas.GRASS_CENTER_COL,
            GardenTileAtlas.GRASS_CENTER_ROW,
            ts,
            GardenTileAtlas.GRASS_FILL_ARGB);

    tileImages[GardenTileAtlas.TILE_GRASS_VAR] =
        ResourceLoader.extractSolidTile(
            tilemapSheet,
            GardenTileAtlas.GRASS_VAR_COL,
            GardenTileAtlas.GRASS_VAR_ROW,
            ts,
            GardenTileAtlas.GRASS_FILL_ARGB);

    tileImages[GardenTileAtlas.TILE_DIRT] =
        ResourceLoader.extractSolidTile(
            tilemapSheet,
            GardenTileAtlas.PATH_CENTER_COL,
            GardenTileAtlas.PATH_CENTER_ROW,
            ts,
            GardenTileAtlas.PATH_FILL_ARGB);

    tileImages[GardenTileAtlas.TILE_WATER] =
        ResourceLoader.extractSolidTile(
            tilemapSheet,
            GardenTileAtlas.WATER_CENTER_COL,
            GardenTileAtlas.WATER_CENTER_ROW,
            ts,
            GardenTileAtlas.WATER_FILL_ARGB);

    tileImages[GardenTileAtlas.TILE_BED_TOP_LEFT] =
        ResourceLoader.extractSolidTile(
            tilemapSheet,
            GardenTileAtlas.BED_TOP_LEFT_COL,
            GardenTileAtlas.BED_TOP_LEFT_ROW,
            ts,
            GardenTileAtlas.BED_FILL_ARGB);
    tileImages[GardenTileAtlas.TILE_BED_TOP_RIGHT] =
        ResourceLoader.extractSolidTile(
            tilemapSheet,
            GardenTileAtlas.BED_TOP_RIGHT_COL,
            GardenTileAtlas.BED_TOP_RIGHT_ROW,
            ts,
            GardenTileAtlas.BED_FILL_ARGB);
    tileImages[GardenTileAtlas.TILE_BED_BOTTOM_LEFT] =
        ResourceLoader.extractSolidTile(
            tilemapSheet,
            GardenTileAtlas.BED_BOTTOM_LEFT_COL,
            GardenTileAtlas.BED_BOTTOM_LEFT_ROW,
            ts,
            GardenTileAtlas.BED_FILL_ARGB);
    tileImages[GardenTileAtlas.TILE_BED_BOTTOM_RIGHT] =
        ResourceLoader.extractSolidTile(
            tilemapSheet,
            GardenTileAtlas.BED_BOTTOM_RIGHT_COL,
            GardenTileAtlas.BED_BOTTOM_RIGHT_ROW,
            ts,
            GardenTileAtlas.BED_FILL_ARGB);

    System.out.println("Loaded " + tileImages.length + " tile types");
  }

  private void loadDecorationSheets() {
    System.out.println("Loading decoration spritesheets...");
    itemsSheet = ResourceLoader.loadImage(ObjectSpriteAtlas.ITEMS_SHEET);
    miscSheet = ResourceLoader.loadImage(ObjectSpriteAtlas.MISC_SHEET);
    System.out.println("Decoration sheets loaded");
  }

  // ── Map generation ────────────────────────────────────────────────────

  private void generateFarmMap() {
    for (int row = 0; row < height; row++) {
      for (int col = 0; col < width; col++) {
        tiles[row][col] = GardenTileAtlas.TILE_GRASS;
      }
    }

    sprinkleGrassVariation();
    layPaths();
    placeGardenBedGrid();
    placeDecorations();
  }

  private void sprinkleGrassVariation() {
    int[][] spots = {{2, 3}, {17, 3}, {2, 16}, {17, 16}, {5, 5}, {14, 5}, {5, 15}, {14, 15}};
    for (int[] spot : spots) {
      tiles[spot[1]][spot[0]] = GardenTileAtlas.TILE_GRASS_VAR;
    }
  }

  private void layPaths() {
    // Courtyard in front of the house.
    for (int row = 3; row <= 4; row++) {
      for (int col = 8; col <= 11; col++) {
        tiles[row][col] = GardenTileAtlas.TILE_DIRT;
      }
    }

    // Main north–south walkway (player walks here).
    for (int row = 5; row <= 16; row++) {
      tiles[row][9] = GardenTileAtlas.TILE_DIRT;
      tiles[row][10] = GardenTileAtlas.TILE_DIRT;
    }

    // Short connectors to beds beside the player (row 11).
    for (int col = 6; col <= 8; col++) {
      tiles[11][col] = GardenTileAtlas.TILE_DIRT;
    }
    for (int col = 11; col <= 13; col++) {
      tiles[11][col] = GardenTileAtlas.TILE_DIRT;
    }

    // Upper field connectors.
    for (int col = 6; col <= 8; col++) {
      tiles[8][col] = GardenTileAtlas.TILE_DIRT;
    }
    for (int col = 11; col <= 13; col++) {
      tiles[8][col] = GardenTileAtlas.TILE_DIRT;
    }
  }

  /** Four 2x2 beds — two next to spawn, two further north. */
  private void placeGardenBedGrid() {
    placeGardenBed(7, 10);
    placeGardenBed(11, 10);
    placeGardenBed(7, 7);
    placeGardenBed(11, 7);
  }

  private void placeGardenBed(int topCol, int topRow) {
    tiles[topRow][topCol] = GardenTileAtlas.TILE_BED_TOP_LEFT;
    tiles[topRow][topCol + 1] = GardenTileAtlas.TILE_BED_TOP_RIGHT;
    tiles[topRow + 1][topCol] = GardenTileAtlas.TILE_BED_BOTTOM_LEFT;
    tiles[topRow + 1][topCol + 1] = GardenTileAtlas.TILE_BED_BOTTOM_RIGHT;
  }

  // ── Decoration placement ──────────────────────────────────────────────

  private void placeDecorations() {
    placeBuildings();
    placeTrees();
    placeUtilityObjects();
  }

  private void placeBuildings() {
    double scale = 1.85;
    tallDecorations.add(
        new MapDecoration(
            itemsSheet,
            ObjectSpriteAtlas.HOUSE_SRC_X,
            ObjectSpriteAtlas.HOUSE_SRC_Y,
            ObjectSpriteAtlas.HOUSE_SRC_W,
            ObjectSpriteAtlas.HOUSE_SRC_H,
            7.5 * TILE_PX,
            0.0,
            ObjectSpriteAtlas.HOUSE_SRC_W * scale,
            ObjectSpriteAtlas.HOUSE_SRC_H * scale,
            true));
  }

  private void placeTrees() {
    double scale = 2.0;
    int tw = (int) (ObjectSpriteAtlas.GREEN_TREE_SRC_W * scale);
    int th = (int) (ObjectSpriteAtlas.GREEN_TREE_SRC_H * scale);

    addTree(
        ObjectSpriteAtlas.GREEN_TREE_SRC_X, ObjectSpriteAtlas.GREEN_TREE_SRC_Y, 1.5, 1.5, tw, th);
    addTree(ObjectSpriteAtlas.RED_TREE_SRC_X, ObjectSpriteAtlas.RED_TREE_SRC_Y, 17.0, 1.5, tw, th);
    addTree(
        ObjectSpriteAtlas.GREEN_TREE_SRC_X, ObjectSpriteAtlas.GREEN_TREE_SRC_Y, 1.5, 16.5, tw, th);
    addTree(ObjectSpriteAtlas.RED_TREE_SRC_X, ObjectSpriteAtlas.RED_TREE_SRC_Y, 17.0, 16.5, tw, th);
  }

  private void addTree(int srcX, int srcY, double col, double row, int w, int h) {
    tallDecorations.add(
        new MapDecoration(
            itemsSheet,
            srcX,
            srcY,
            ObjectSpriteAtlas.GREEN_TREE_SRC_W,
            ObjectSpriteAtlas.GREEN_TREE_SRC_H,
            col * TILE_PX,
            row * TILE_PX,
            w,
            h,
            true));
  }

  private void placeUtilityObjects() {
    shopBench =
        new MapDecoration(
            miscSheet,
            ObjectSpriteAtlas.BENCH_SRC_X,
            ObjectSpriteAtlas.BENCH_SRC_Y,
            ObjectSpriteAtlas.BENCH_SRC_W,
            ObjectSpriteAtlas.BENCH_SRC_H,
            12 * TILE_PX + 4,
            4 * TILE_PX + 16,
            64,
            32,
            true);
    groundDecorations.add(shopBench);

    groundDecorations.add(
        new MapDecoration(
            itemsSheet,
            ObjectSpriteAtlas.CHEST_SRC_X,
            ObjectSpriteAtlas.CHEST_SRC_Y,
            ObjectSpriteAtlas.CHEST_SRC_W,
            ObjectSpriteAtlas.CHEST_SRC_H,
            10 * TILE_PX + 8,
            4 * TILE_PX + 12,
            28,
            28,
            true));
  }

  // ── Test plants ───────────────────────────────────────────────────────

  public void addTestPlants(PlantManager plantManager, PlantType plantType) {
    System.out.println("Adding test plants to garden beds...");
    // Plant on first few beds (top-left corners of 2x2 beds)
    // Demo plants on the two beds beside the spawn walkway.
    plantManager.plantSeed(plantType, 7, 10);
    plantManager.plantSeed(plantType, 11, 10);
    int count = 2;
    System.out.println("Added " + count + " test plants");
  }

  // ── Rendering ─────────────────────────────────────────────────────────

  public void render(GraphicsContext gc, double offsetX, double offsetY) {
    if (!isLoaded) {
      return;
    }

    gc.setImageSmoothing(false);
    double snappedOffsetX = Math.round(offsetX);
    double snappedOffsetY = Math.round(offsetY);

    // Render base tiles
    for (int row = 0; row < height; row++) {
      for (int col = 0; col < width; col++) {
        int tileId = tiles[row][col];
        if (tileId >= 0 && tileId < tileImages.length && tileImages[tileId] != null) {
          double screenX = Math.round(orthoCoords.toScreenX(col, row) + snappedOffsetX);
          double screenY = Math.round(orthoCoords.toScreenY(col, row) + snappedOffsetY);
          gc.drawImage(tileImages[tileId], screenX, screenY, 48.0, 48.0);
        }
      }
    }

    // Render ground-level decorations (flowers, stones, paths)
    for (MapDecoration deco : groundDecorations) {
      deco.render(gc, snappedOffsetX, snappedOffsetY);
    }

    gc.setImageSmoothing(true);
  }

  // ── Queries ───────────────────────────────────────────────────────────

  public boolean isWalkable(int col, int row) {
    if (col < 0 || col >= width || row < 0 || row >= height) {
      return false;
    }
    int tileId = tiles[row][col];
    if (tileId == GardenTileAtlas.TILE_WATER) {
      return false;
    }
    // Check if any blocking decoration covers this tile
    for (MapDecoration deco : tallDecorations) {
      if (deco.occupiesTile(col, row, TILE_PX)) {
        return false;
      }
    }
    for (MapDecoration deco : groundDecorations) {
      if (deco.occupiesTile(col, row, TILE_PX)) {
        return false;
      }
    }
    return true;
  }

  public boolean isShopTile(int col, int row) {
    return shopBench != null && shopBench.occupiesTile(col, row, TILE_PX);
  }

  public List<MapDecoration> getTallDecorations() {
    return tallDecorations;
  }

  public boolean isLoaded() {
    return isLoaded;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public OrthogonalCoordinates getOrthoCoords() {
    return orthoCoords;
  }

  public int getTileId(int col, int row) {
    if (col < 0 || col >= width || row < 0 || row >= height) {
      return -1;
    }
    return tiles[row][col];
  }
}
