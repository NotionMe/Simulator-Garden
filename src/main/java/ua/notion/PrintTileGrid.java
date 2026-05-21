package ua.notion;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class PrintTileGrid {
  public static void main(String[] args) {
    try {
      File file = new File("src/main/resources/assets/tilemaps/spring tilemap.png");
      if (!file.exists()) {
        System.out.println("File not found!");
        return;
      }
      BufferedImage img = ImageIO.read(file);
      int cols = img.getWidth() / 16;
      int rows = img.getHeight() / 16;
      System.out.println("Grid layout of spring tilemap (" + cols + "x" + rows + "):");

      for (int r = 0; r < rows; r++) {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < cols; c++) {
          int opaqueCount = 0;
          int greenCount = 0;
          int brownCount = 0;
          int blueCount = 0;
          int greyCount = 0;

          for (int ty = 0; ty < 16; ty++) {
            for (int tx = 0; tx < 16; tx++) {
              int px = c * 16 + tx;
              int py = r * 16 + ty;
              if (px < img.getWidth() && py < img.getHeight()) {
                int argb = img.getRGB(px, py);
                int alpha = (argb >>> 24) & 0xff;
                if (alpha > 50) {
                  opaqueCount++;
                  int red = (argb >>> 16) & 0xff;
                  int green = (argb >>> 8) & 0xff;
                  int blue = argb & 0xff;

                  // Classify color
                  if (blue > green && blue > red && blue > 100) {
                    blueCount++;
                  } else if (green > red && green > blue) {
                    greenCount++;
                  } else if (red > blue && green > blue && (red > 120 || green > 100)) {
                    // Brown/Orange
                    brownCount++;
                  } else {
                    greyCount++;
                  }
                }
              }
            }
          }

          if (opaqueCount < 20) {
            sb.append("[  Empty   ] ");
          } else {
            // Classify the whole tile based on dominant colors
            if (blueCount > opaqueCount * 0.7) {
              sb.append("[  Water   ] ");
            } else if (greenCount > opaqueCount * 0.7) {
              sb.append("[  Grass   ] ");
            } else if (brownCount > opaqueCount * 0.7) {
              sb.append("[  Dirt    ] ");
            } else if (greyCount > opaqueCount * 0.7) {
              sb.append("[  Stone   ] ");
            } else {
              // It's a transition tile
              String dominant = "";
              if (greenCount > brownCount && greenCount > blueCount) dominant = "G";
              else if (brownCount > greenCount && brownCount > blueCount) dominant = "D";
              else if (blueCount > greenCount && blueCount > brownCount) dominant = "W";
              else dominant = "X";
              sb.append(String.format("[Trans-%s:%02d] ", dominant, opaqueCount * 10 / 256));
            }
          }
        }
        System.out.println("Row " + String.format("%02d", r) + ": " + sb.toString());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
