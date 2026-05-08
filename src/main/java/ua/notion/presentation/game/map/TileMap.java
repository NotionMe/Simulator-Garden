package ua.notion.presentation.game.map;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import ua.notion.infrastructure.async.AsyncExecutor;
import ua.notion.presentation.game.GameConstants;
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
  private IsometricCoordinates isoCoords;
  private boolean isLoaded = false;

  public TileMap(int width, int height) {
    this.width = width;
    this.height = height;
    this.tiles = new int[height][width];
    this.objects = new ArrayList<>();

    this.isoCoords =
        new IsometricCoordinates(
            GardenTileAtlas.BLOCK_TILE_WIDTH, GardenTileAtlas.BLOCK_TILE_HEIGHT);
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
    System.out.println("Loading isometric garden tiles...");

    Image blocksSpritesheet = ResourceLoader.loadImage(GardenTileAtlas.BLOCKS_SPRITE_PATH);

    tileImages = new Image[20];

    tileImages[0] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.GRASS_1);
    tileImages[1] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.GRASS_2);
    tileImages[2] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.GRASS_3);
    tileImages[3] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.GRASS_4);

    tileImages[4] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.DIRT_1);
    tileImages[5] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.DIRT_2);
    tileImages[6] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.DIRT_3);
    tileImages[7] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.DIRT_4);

    tileImages[8] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.GARDEN_BED_1);
    tileImages[9] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.GARDEN_BED_2);
    tileImages[10] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.GARDEN_BED_3);
    tileImages[11] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.GARDEN_BED_4);

    tileImages[12] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.WATER_1);
    tileImages[13] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.WATER_2);
    tileImages[14] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.STONE_1);
    tileImages[15] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.STONE_2);

    tileImages[16] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.BLOCK_5_1);
    tileImages[17] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.BLOCK_5_2);
    tileImages[18] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.BLOCK_5_3);
    tileImages[19] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.BLOCK_5_4);

    System.out.println("Loaded " + tileImages.length + " isometric tiles");
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

  public void render(GraphicsContext gc) {
    if (!isLoaded) {
      return;
    }

    double offsetX = GameConstants.MAP_OFFSET_X;
    double offsetY = GameConstants.MAP_OFFSET_Y;

    for (int row = 0; row < height; row++) {
      for (int col = 0; col < width; col++) {
        int tileId = tiles[row][col];

        if (tileId >= 0 && tileId < tileImages.length) {
          double screenX = isoCoords.toScreenX(col, row) + offsetX;
          double screenY = isoCoords.toScreenY(col, row) + offsetY;

          gc.drawImage(
              tileImages[tileId],
              screenX,
              screenY,
              GardenTileAtlas.BLOCK_TILE_WIDTH * 1.5,
              GardenTileAtlas.BLOCK_TILE_HEIGHT * 1.5);
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

  public IsometricCoordinates getIsoCoords() {
    return isoCoords;
  }
}
