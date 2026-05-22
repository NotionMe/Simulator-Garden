package ua.notion.presentation.game.util;

import java.io.InputStream;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import ua.notion.presentation.game.assets.TileCoordinate;

public class ResourceLoader {
  private static final String ASSETS_PATH = "/assets/";

  private ResourceLoader() {
    throw new AssertionError("Cannot instantiate utility class");
  }

  public static Image loadImage(String path) {
    try {
      InputStream stream = ResourceLoader.class.getResourceAsStream(ASSETS_PATH + path);
      if (stream == null) {
        System.err.println("Asset not found: " + path);
        return createPlaceholder(16, 16);
      }
      return new Image(stream);
    } catch (Exception e) {
      System.err.println("Failed to load image: " + path);
      e.printStackTrace();
      return createPlaceholder(16, 16);
    }
  }

  public static Image extractTile(Image spritesheet, TileCoordinate coord) {
    try {
      PixelReader reader = spritesheet.getPixelReader();
      WritableImage tile = new WritableImage(coord.getWidth(), coord.getHeight());
      tile.getPixelWriter()
          .setPixels(0, 0, coord.getWidth(), coord.getHeight(), reader, coord.getX(), coord.getY());
      return tile;
    } catch (Exception e) {
      System.err.println("Failed to extract tile at " + coord);
      e.printStackTrace();
      return createPlaceholder(coord.getWidth(), coord.getHeight());
    }
  }

  public static Image extractTile(Image tilemap, int tileX, int tileY, int tileSize) {
    return extractTile(
        tilemap, new TileCoordinate(tileX * tileSize, tileY * tileSize, tileSize, tileSize));
  }

  /** Extract tile and replace transparent pixels so the sky does not show through gaps. */
  public static Image extractSolidTile(
      Image tilemap, int tileX, int tileY, int tileSize, int fillArgb) {
    try {
      PixelReader reader = tilemap.getPixelReader();
      WritableImage tile = new WritableImage(tileSize, tileSize);
      int baseX = tileX * tileSize;
      int baseY = tileY * tileSize;
      for (int y = 0; y < tileSize; y++) {
        for (int x = 0; x < tileSize; x++) {
          int argb = reader.getArgb(baseX + x, baseY + y);
          int alpha = (argb >>> 24) & 0xff;
          tile.getPixelWriter().setArgb(x, y, alpha < 128 ? fillArgb : argb);
        }
      }
      return tile;
    } catch (Exception e) {
      System.err.println("Failed to extract solid tile at " + tileX + "," + tileY);
      e.printStackTrace();
      return createPlaceholder(tileSize, tileSize);
    }
  }

  private static Image createPlaceholder(int width, int height) {
    WritableImage img = new WritableImage(width, height);
    var pw = img.getPixelWriter();
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        pw.setArgb(x, y, 0xFFFF00FF);
      }
    }
    return img;
  }
}
