package ua.notion.presentation.game.map;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import ua.notion.infrastructure.async.AsyncExecutor;
import ua.notion.presentation.game.assets.GardenTileAtlas;
import ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType;
import ua.notion.presentation.game.plant.PlantManager;
import ua.notion.presentation.game.util.ResourceLoader;

public class TileMap {
  private final int width;
  private final int height;
  private final int[][] tiles;
  private final List<MapObject> objects;

  private Image[] tileImages;
  private OrthogonalCoordinates orthoCoords;
  private boolean isLoaded = false;

  public TileMap(int width, int height) {
    this.width = width;
    this.height = height;
    this.tiles = new int[height][width];
    this.objects = new ArrayList<>();

    this.orthoCoords = new OrthogonalCoordinates(48, 48);
  }

  public CompletableFuture<Void> loadAsync() {
    return AsyncExecutor.runAsync(
            () -> {
              loadTiles();
              generateParkMap();
            })
        .thenRun(
            () -> {
              isLoaded = true;
              System.out.println("TileMap loaded successfully");
            });
  }

  private void loadTiles() {
    System.out.println("Loading top-down garden tiles...");

    Image tilemapSheet = ResourceLoader.loadImage(GardenTileAtlas.SPRING_TILEMAP_PATH);

    tileImages = new Image[5];

    tileImages[GardenTileAtlas.TILE_GRASS] =
        ResourceLoader.extractTile(
            tilemapSheet,
            GardenTileAtlas.GRASS_CENTER_COL,
            GardenTileAtlas.GRASS_CENTER_ROW,
            GardenTileAtlas.TILE_SIZE);

    tileImages[GardenTileAtlas.TILE_BED_TOP_LEFT] =
        ResourceLoader.extractSolidTile(
            tilemapSheet,
            GardenTileAtlas.BED_TOP_LEFT_COL,
            GardenTileAtlas.BED_TOP_LEFT_ROW,
            GardenTileAtlas.TILE_SIZE,
            GardenTileAtlas.BED_FILL_ARGB);
    tileImages[GardenTileAtlas.TILE_BED_TOP_RIGHT] =
        ResourceLoader.extractSolidTile(
            tilemapSheet,
            GardenTileAtlas.BED_TOP_RIGHT_COL,
            GardenTileAtlas.BED_TOP_RIGHT_ROW,
            GardenTileAtlas.TILE_SIZE,
            GardenTileAtlas.BED_FILL_ARGB);
    tileImages[GardenTileAtlas.TILE_BED_BOTTOM_LEFT] =
        ResourceLoader.extractSolidTile(
            tilemapSheet,
            GardenTileAtlas.BED_BOTTOM_LEFT_COL,
            GardenTileAtlas.BED_BOTTOM_LEFT_ROW,
            GardenTileAtlas.TILE_SIZE,
            GardenTileAtlas.BED_FILL_ARGB);
    tileImages[GardenTileAtlas.TILE_BED_BOTTOM_RIGHT] =
        ResourceLoader.extractSolidTile(
            tilemapSheet,
            GardenTileAtlas.BED_BOTTOM_RIGHT_COL,
            GardenTileAtlas.BED_BOTTOM_RIGHT_ROW,
            GardenTileAtlas.TILE_SIZE,
            GardenTileAtlas.BED_FILL_ARGB);

    System.out.println("Loaded " + tileImages.length + " top-down 16x16 tiles");
  }

  private void generateParkMap() {
    for (int row = 0; row < height; row++) {
      for (int col = 0; col < width; col++) {
        tiles[row][col] = GardenTileAtlas.TILE_GRASS;
      }
    }

    addGardenBeds();

    System.out.println("Park map generated with grass fill and garden beds");
  }

  private void placeGardenBed(int topCol, int topRow) {
    tiles[topRow][topCol] = GardenTileAtlas.TILE_BED_TOP_LEFT;
    tiles[topRow][topCol + 1] = GardenTileAtlas.TILE_BED_TOP_RIGHT;
    tiles[topRow + 1][topCol] = GardenTileAtlas.TILE_BED_BOTTOM_LEFT;
    tiles[topRow + 1][topCol + 1] = GardenTileAtlas.TILE_BED_BOTTOM_RIGHT;
  }

  private void addGardenBeds() {
    placeGardenBed(5, 5);
    placeGardenBed(10, 5);
    placeGardenBed(5, 10);
    placeGardenBed(10, 10);
    placeGardenBed(8, 15);
  }

  public void addTestPlants(PlantManager plantManager, PlantType plantType) {
    System.out.println("Adding test plants to garden beds...");
    plantManager.plantSeed(plantType, 5, 5);
    plantManager.plantSeed(plantType, 5, 10);
    plantManager.plantSeed(plantType, 10, 5);
    plantManager.plantSeed(plantType, 10, 10);
    plantManager.plantSeed(plantType, 8, 15);
    System.out.println("Added 5 test plants");
  }

  public void render(GraphicsContext gc, double offsetX, double offsetY) {
    if (!isLoaded) {
      return;
    }

    gc.setImageSmoothing(false);

    double snappedOffsetX = Math.round(offsetX);
    double snappedOffsetY = Math.round(offsetY);

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

    gc.setImageSmoothing(true);
  }

  public boolean isWalkable(int col, int row) {
    if (col < 0 || col >= width || row < 0 || row >= height) {
      return false;
    }
    return true;
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
