package ua.notion.presentation.game.map;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import ua.notion.presentation.game.GameScene;
import ua.notion.presentation.game.assets.GardenTileAtlas;
import ua.notion.presentation.game.util.ResourceLoader;

public class TileMap {
  private final int width;
  private final int height; // висота в тайлах (рядки)
  private final int[][] tiles; // ідентифікатори тайлів: map[row][col]
  private final List<MapObject> objects; // декоративні об'єкти

  // Кешовані зображення тайлів
  private Image[] tileImages; // 0-19: всі доступні тайли з атласу

  // Ізометрична система координат
  private IsometricCoordinates isoCoords;

  public TileMap(int width, int height) {
    this.width = width; // columns
    this.height = height; // rows
    this.tiles = new int[height][width]; // [row][col]
    this.objects = new ArrayList<>();

    this.isoCoords =
        new IsometricCoordinates(
            GardenTileAtlas.BLOCK_TILE_WIDTH, GardenTileAtlas.BLOCK_TILE_HEIGHT);

    loadTiles();
    generateParkMap();
  }

  /** Load tile sprites from garden atlas */
  private void loadTiles() {
    System.out.println("Loading isometric garden tiles...");

    Image blocksSpritesheet = ResourceLoader.loadImage(GardenTileAtlas.BLOCKS_SPRITE_PATH);

    // Load all 20 tile types (0-19)
    tileImages = new Image[20];

    // Row 0: Grass variations (0-3)
    tileImages[0] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.GRASS_1);
    tileImages[1] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.GRASS_2);
    tileImages[2] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.GRASS_3);
    tileImages[3] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.GRASS_4);

    // Row 1: Dirt/soil variations (4-7)
    tileImages[4] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.DIRT_1);
    tileImages[5] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.DIRT_2);
    tileImages[6] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.DIRT_3);
    tileImages[7] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.DIRT_4);

    // Row 2: Garden beds (8-11)
    tileImages[8] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.GARDEN_BED_1);
    tileImages[9] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.GARDEN_BED_2);
    tileImages[10] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.GARDEN_BED_3);
    tileImages[11] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.GARDEN_BED_4);

    // Row 3: Water/stone (12-15)
    tileImages[12] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.WATER_1);
    tileImages[13] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.WATER_2);
    tileImages[14] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.STONE_1);
    tileImages[15] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.STONE_2);

    // Row 4: Additional blocks (16-19)
    tileImages[16] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.BLOCK_5_1);
    tileImages[17] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.BLOCK_5_2);
    tileImages[18] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.BLOCK_5_3);
    tileImages[19] = ResourceLoader.extractTile(blocksSpritesheet, GardenTileAtlas.BLOCK_5_4);

    System.out.println("Loaded " + tileImages.length + " isometric tiles");
  }

  /** Generate park map with stone path, trees, benches, and flowers */
  private void generateParkMap() {
    // Park map 15 rows × 10 columns
    // Карта з водоймою, грядками та землею
    // 0-3: трава, 4-7: dirt, 8-11: грядки, 12-13: вода, 14-15: камінь, 16-19: земля
    int[][] parkTiles = {
      {5, 0, 0, 1, 1, 1, 0, 0, 0, 0}, // row 0 - трава
      {0, 0, 14, 14, 14, 1, 1, 0, 0, 0}, // row 1 - кам'яна доріжка
      {0, 1, 14, 16, 14, 1, 2, 1, 0, 0}, // row 2 - доріжка + земля
      {1, 1, 14, 16, 14, 14, 14, 1, 1, 0}, // row 3 - доріжка
      {1, 2, 14, 16, 17, 17, 14, 1, 2, 1}, // row 4 - земля 16-19
      {1, 12, 12, 17, 17, 18, 14, 14, 14, 1}, // row 5 - водойма + земля
      {2, 12, 13, 12, 18, 18, 4, 5, 14, 1}, // row 6 - вода + dirt 4-7
      {2, 1, 12, 12, 4, 5, 5, 6, 14, 2}, // row 7 - край води + dirt
      {2, 1, 1, 8, 9, 10, 14, 14, 14, 2}, // row 8 - грядки 8-11
      {2, 1, 2, 9, 10, 11, 14, 2, 3, 2}, // row 9 - грядки + доріжка
      {2, 2, 1, 10, 11, 14, 14, 2, 3, 3}, // row 10 - грядки
      {2, 3, 2, 14, 14, 2, 3, 3, 2, 2}, // row 11 - трава
      {3, 2, 14, 14, 3, 2, 2, 3, 2, 3}, // row 12 - доріжка
      {3, 14, 14, 2, 3, 2, 3, 2, 3, 2}, // row 13 - доріжка
      {14, 14, 3, 2, 2, 3, 2, 2, 3, 2}, // row 14 - вихід
    };

    // Copy tiles
    for (int row = 0; row < height && row < parkTiles.length; row++) {
      for (int col = 0; col < width && col < parkTiles[row].length; col++) {
        tiles[row][col] = parkTiles[row][col];
      }
    }

    System.out.println("Park map generated: " + objects.size() + " decorative objects");
  }

  /** Render the isometric map to canvas */
  public void render(GraphicsContext gc) {
    // Calculate offset to center the map
    double offsetX = GameScene.CANVAS_WIDTH / 2.0 - GardenTileAtlas.BLOCK_TILE_WIDTH / 2.0;
    double offsetY = 100; // Top padding

    // Render tiles in correct isometric order (back to front)
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

    // TODO: Render objects (trees, benches, flowers) - need sprites
  }

  public boolean isWalkable(int col, int row) {
    if (col < 0 || col >= width || row < 0 || row >= height) {
      return false;
    }
    int tileId = tiles[row][col];
    // Water (12,13) is not walkable
    return tileId != 12 && tileId != 13;
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
