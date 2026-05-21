package ua.notion;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class BorderInspector {
  public static void main(String[] args) {
    try {
      File file = new File("src/main/resources/assets/tilemaps/spring tilemap.png");
      if (!file.exists()) {
        System.out.println("File not found!");
        return;
      }
      BufferedImage img = ImageIO.read(file);
      // Let's print the colors of pixels in row 8 (which is grass/dirt) around x = 14, 15, 16, 17,
      // 18
      int y = 8 * 16 + 8; // middle of row 8
      System.out.println("Colors at row 8 middle (y=" + y + ") around x=16:");
      for (int x = 12; x <= 20; x++) {
        int rgb = img.getRGB(x, y);
        System.out.printf("x=%d: #%06x (alpha=%d)\n", x, rgb & 0xffffff, (rgb >>> 24) & 0xff);
      }

      System.out.println("Colors at row 8 middle around x=32:");
      for (int x = 28; x <= 36; x++) {
        int rgb = img.getRGB(x, y);
        System.out.printf("x=%d: #%06x (alpha=%d)\n", x, rgb & 0xffffff, (rgb >>> 24) & 0xff);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
