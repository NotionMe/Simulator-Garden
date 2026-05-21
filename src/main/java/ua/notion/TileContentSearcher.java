package ua.notion;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;

public class TileContentSearcher {
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

      System.out.println("Detailed tile color palettes (col,row):");

      for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {
          Map<String, Integer> colorCounts = new HashMap<>();
          int opaquePixels = 0;

          for (int ty = 0; ty < 16; ty++) {
            for (int tx = 0; tx < 16; tx++) {
              int px = c * 16 + tx;
              int py = r * 16 + ty;
              int argb = img.getRGB(px, py);
              int alpha = (argb >>> 24) & 0xff;
              if (alpha > 100) {
                opaquePixels++;
                int red = (argb >>> 16) & 0xff;
                int green = (argb >>> 8) & 0xff;
                int blue = argb & 0xff;

                // Group similar colors together
                int groupR = (red / 20) * 20;
                int groupG = (green / 20) * 20;
                int groupB = (blue / 20) * 20;
                String key = String.format("#%02x%02x%02x", groupR, groupG, groupB);
                colorCounts.put(key, colorCounts.getOrDefault(key, 0) + 1);
              }
            }
          }

          if (opaquePixels > 50) {
            List<Map.Entry<String, Integer>> sortedColors = new ArrayList<>(colorCounts.entrySet());
            sortedColors.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(3, sortedColors.size()); i++) {
              sb.append(sortedColors.get(i).getKey())
                  .append(":")
                  .append(sortedColors.get(i).getValue() * 100 / opaquePixels)
                  .append("% ");
            }
            System.out.println(
                String.format(
                    "Tile (%d,%d): Opaque=%d, Palette: %s", c, r, opaquePixels, sb.toString()));
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
