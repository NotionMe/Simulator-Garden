package ua.notion;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CheckImageSizes {
  public static void main(String[] args) {
    String[] files = {
      "autumn tilemap.png",
      "bridges.png",
      "inside.png",
      "spring tilemap.png",
      "summer tilemap.png",
      "winter outside.png"
    };
    for (String filename : files) {
      try {
        File file = new File("src/main/resources/assets/tilemaps/" + filename);
        if (file.exists()) {
          BufferedImage img = ImageIO.read(file);
          System.out.println(filename + ": " + img.getWidth() + "x" + img.getHeight());
        } else {
          System.out.println(filename + ": NOT FOUND");
        }
      } catch (Exception e) {
        System.out.println(filename + ": ERROR " + e.getMessage());
      }
    }
  }
}
