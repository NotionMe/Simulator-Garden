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

    tileImages = new Image[20];

    // Solid grass tiles (Row 3, Row 4 cols 0-1)
    tileImages[0] = ResourceLoader.extractTile(tilemapSheet, 0, 3, 16);
    tileImages[1] = ResourceLoader.extractTile(tilemapSheet, 1, 3, 16);
    tileImages[2] = ResourceLoader.extractTile(tilemapSheet, 0, 4, 16);
    tileImages[3] = ResourceLoader.extractTile(tilemapSheet, 1, 4, 16);

    // Solid pathway/dirt tiles (Row 11, Row 12 cols 0-1)
    tileImages[4] = ResourceLoader.extractTile(tilemapSheet, 0, 11, 16);
    tileImages[5] = ResourceLoader.extractTile(tilemapSheet, 1, 11, 16);
    tileImages[6] = ResourceLoader.extractTile(tilemapSheet, 0, 12, 16);
    tileImages[7] = ResourceLoader.extractTile(tilemapSheet, 1, 12, 16);

    // Solid garden bed / tilled soil tiles (Row 10 cols 0-3)
    tileImages[8] = ResourceLoader.extractTile(tilemapSheet, 0, 10, 16);
    tileImages[9] = ResourceLoader.extractTile(tilemapSheet, 1, 10, 16);
    tileImages[10] = ResourceLoader.extractTile(tilemapSheet, 2, 10, 16);
    tileImages[11] = ResourceLoader.extractTile(tilemapSheet, 3, 10, 16);

    // Solid water tiles (Row 8 col 8, Row 9 col 8)
    tileImages[12] = ResourceLoader.extractTile(tilemapSheet, 8, 8, 16);
    tileImages[13] = ResourceLoader.extractTile(tilemapSheet, 8, 9, 16);

    // Solid stone/rock tiles (Row 8 cols 0-1)
    tileImages[14] = ResourceLoader.extractTile(tilemapSheet, 0, 8, 16);
    tileImages[15] = ResourceLoader.extractTile(tilemapSheet, 1, 8, 16);

    // Other solid blocks/obstacles (Row 8, Row 9 cols 2-3)
    tileImages[16] = ResourceLoader.extractTile(tilemapSheet, 2, 8, 16);
    tileImages[17] = ResourceLoader.extractTile(tilemapSheet, 3, 8, 16);
    tileImages[18] = ResourceLoader.extractTile(tilemapSheet, 2, 9, 16);
    tileImages[19] = ResourceLoader.extractTile(tilemapSheet, 3, 9, 16);

    System.out.println("Loaded " + tileImages.length + " top-down 16x16 tiles");
  }

  private void generateParkMap() {
    for (int row = 0; row < height; row++) {
      for (int col = 0; col < width; col++) {
        if (row == 0 || row == height - 1 || col == 0 || col == width - 1) {
          tiles[row][col] = 12;
        } else {
          tiles[row][col] = 0;
        }
      }
    }

    addGardenBeds();

    System.out.println("Park map generated with water borders and garden beds");
  }

  private void addGardenBeds() {
    tiles[5][5] = 10;
    tiles[5][6] = 10;
    tiles[6][5] = 10;
    tiles[6][6] = 10;

    tiles[5][10] = 10;
    tiles[5][11] = 10;
    tiles[6][10] = 10;
    tiles[6][11] = 10;

    tiles[10][5] = 10;
    tiles[10][6] = 10;
    tiles[11][5] = 10;
    tiles[11][6] = 10;

    tiles[10][10] = 10;
    tiles[10][11] = 10;
    tiles[11][10] = 10;
    tiles[11][11] = 10;

    tiles[15][8] = 10;
    tiles[15][9] = 10;
    tiles[16][8] = 10;
    tiles[16][9] = 10;
  }

  public void addTestPlants(PlantManager plantManager, PlantType plantType) {
    System.out.println("Adding test plants to garden beds...");
    plantManager.plantSeed(plantType, 5, 5);
    plantManager.plantSeed(plantType, 5, 10);
    plantManager.plantSeed(plantType, 10, 5);
    plantManager.plantSeed(plantType, 10, 10);
    plantManager.plantSeed(plantType, 15, 8);
    System.out.println("Added 5 test plants");
  }

  public void render(GraphicsContext gc, double offsetX, double offsetY) {
    if (!isLoaded) {
      return;
    }

    for (int row = 0; row < height; row++) {
      for (int col = 0; col < width; col++) {
        int tileId = tiles[row][col];

        if (tileId >= 0 && tileId < tileImages.length) {
          double screenX = orthoCoords.toScreenX(col, row) + offsetX;
          double screenY = orthoCoords.toScreenY(col, row) + offsetY;

          gc.drawImage(tileImages[tileId], screenX, screenY, 48.0, 48.0);
        }
      }
    }
  }

  public boolean isWalkable(int col, int row) {
    if (col < 0 || col >= width || row < 0 || row >= height) {
      return false;
    }
    int tileId = tiles[row][col];
    return tileId != 12 && tileId != 13;
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
