package ua.notion.presentation.game.assets;

import java.util.EnumMap;
import java.util.Map;
import javafx.scene.image.Image;
import ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType;
import ua.notion.presentation.game.util.ResourceLoader;

public final class PlantIconCache {

  private static final Map<PlantType, Image> ICONS = new EnumMap<>(PlantType.class);
  private static Image sheet;

  private PlantIconCache() {
    throw new AssertionError("Cannot instantiate utility class");
  }

  public static Image getIcon(PlantType type) {
    return ICONS.computeIfAbsent(type, PlantIconCache::extractIcon);
  }

  /** Ripe crop icon (last growth stage) for inventory slots. */
  public static Image getHarvestIcon(PlantType type) {
    if (sheet == null) {
      sheet = ResourceLoader.loadImage(PlantBasesAtlas.SPRITE_PATH);
    }
    int lastStage = type.getStageCount() - 1;
    int col = type.getStartColumn() + lastStage * type.getColumnStep();
    int row = type.getStartRow();
    return ResourceLoader.extractTile(sheet, col, row, PlantBasesAtlas.SOURCE_CELL_SIZE);
  }

  private static Image extractIcon(PlantType type) {
    if (sheet == null) {
      sheet = ResourceLoader.loadImage(PlantBasesAtlas.SPRITE_PATH);
    }
    int col = type.getStartColumn();
    int row = type.getStartRow();
    return ResourceLoader.extractTile(sheet, col, row, PlantBasesAtlas.SOURCE_CELL_SIZE);
  }
}
