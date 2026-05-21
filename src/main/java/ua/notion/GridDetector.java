package ua.notion;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GridDetector {
  public static void main(String[] args) {
    try {
      File file = new File("src/main/resources/assets/tilemaps/spring tilemap.png");
      if (!file.exists()) {
        System.out.println("File not found!");
        return;
      }
      BufferedImage img = ImageIO.read(file);
      int w = img.getWidth();
      int h = img.getHeight();
      System.out.println("Image dimensions: " + w + "x" + h);

      // Let's test potential tile sizes: 16, 24, 32, 48 etc.
      // Check if there are columns/rows that are completely transparent (alpha = 0).
      System.out.println(
          "Checking vertical transparent lines (x coordinates where all pixels have alpha < 5):");
      for (int x = 0; x < w; x++) {
        boolean allTrans = true;
        for (int y = 0; y < h; y++) {
          int argb = img.getRGB(x, y);
          int alpha = (argb >>> 24) & 0xff;
          if (alpha >= 5) {
            allTrans = false;
            break;
          }
        }
        if (allTrans) {
          System.out.print(x + " ");
        }
      }
      System.out.println();

      System.out.println(
          "Checking horizontal transparent lines (y coordinates where all pixels have alpha < 5):");
      for (int y = 0; y < h; y++) {
        boolean allTrans = true;
        for (int x = 0; x < w; x++) {
          int argb = img.getRGB(x, y);
          int alpha = (argb >>> 24) & 0xff;
          if (alpha >= 5) {
            allTrans = false;
            break;
          }
        }
        if (allTrans) {
          System.out.print(y + " ");
        }
      }
      System.out.println();

      // Let's also print non-transparent pixels profile along X and Y to see the repeating pattern
      // size
      System.out.println(
          "Vertical opaque count profile (column indices with count of opaque pixels):");
      for (int x = 0; x < w; x++) {
        int opaqueCount = 0;
        for (int y = 0; y < h; y++) {
          int argb = img.getRGB(x, y);
          int alpha = (argb >>> 24) & 0xff;
          if (alpha > 10) opaqueCount++;
        }
        System.out.printf("%d:%d ", x, opaqueCount);
      }
      System.out.println();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
