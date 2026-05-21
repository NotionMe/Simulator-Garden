package ua.notion;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class SliceTiles {
  public static void main(String[] args) {
    try {
      File file = new File("src/main/resources/assets/tilemaps/spring tilemap.png");
      if (!file.exists()) {
        System.out.println("File not found!");
        return;
      }
      BufferedImage img = ImageIO.read(file);
      System.out.println("Image size: " + img.getWidth() + "x" + img.getHeight());

      // Create directories for extracted tiles
      File dir16 = new File("src/main/resources/assets/tmp_tiles_16");
      dir16.mkdirs();
      File dir32 = new File("src/main/resources/assets/tmp_tiles_32");
      dir32.mkdirs();

      // Export 16x16
      for (int r = 0; r < img.getHeight() / 16; r++) {
        for (int c = 0; c < img.getWidth() / 16; c++) {
          BufferedImage tile = img.getSubimage(c * 16, r * 16, 16, 16);
          ImageIO.write(tile, "png", new File(dir16, "tile_" + r + "_" + c + ".png"));
        }
      }

      // Export 32x32
      for (int r = 0; r < img.getHeight() / 32; r++) {
        for (int c = 0; c < img.getWidth() / 32; c++) {
          BufferedImage tile = img.getSubimage(c * 32, r * 32, 32, 32);
          ImageIO.write(tile, "png", new File(dir32, "tile_" + r + "_" + c + ".png"));
        }
      }

      System.out.println("Slicing complete!");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
