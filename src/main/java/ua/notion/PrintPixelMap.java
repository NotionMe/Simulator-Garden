package ua.notion;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class PrintPixelMap {
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
      System.out.println("Pixel Map (2x2 scale, 80 columns x 120 rows):");

      for (int y = 0; y < h; y += 2) {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < w; x += 2) {
          int opaqueCount = 0;
          for (int dy = 0; dy < 2; dy++) {
            for (int dx = 0; dx < 2; dx++) {
              int px = x + dx;
              int py = y + dy;
              if (px < w && py < h) {
                int argb = img.getRGB(px, py);
                int alpha = (argb >>> 24) & 0xff;
                if (alpha > 50) {
                  opaqueCount++;
                }
              }
            }
          }
          if (opaqueCount == 0) {
            sb.append(".");
          } else if (opaqueCount < 3) {
            sb.append("-");
          } else {
            sb.append("X");
          }
        }
        System.out.println(String.format("%03d: %s", y, sb.toString()));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
