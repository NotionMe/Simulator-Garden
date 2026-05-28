package ua.notion.presentation.game.plant;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import ua.notion.presentation.game.assets.PlantBasesAtlas;
import ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType;

public class PlantSprite {
  private static Image plantBasesSheet;

  private final PlantType plantType;
  private final int tileX;
  private final int tileY;

  private int stageIndex;
  private double timeInStage;
  private boolean withering;
  private boolean removed;

  public PlantSprite(PlantType plantType, int tileX, int tileY) {
    this.plantType = plantType;
    this.tileX = tileX;
    this.tileY = tileY;
    this.stageIndex = 0;
    this.timeInStage = 0;
    loadSpriteSheet();
  }

  private static void loadSpriteSheet() {
    if (plantBasesSheet == null) {
      plantBasesSheet =
          new Image(
              PlantSprite.class.getResourceAsStream("/assets/" + PlantBasesAtlas.SPRITE_PATH));
    }
  }

  public void update(double deltaSeconds, double growthMultiplier) {
    if (removed) {
      return;
    }

    timeInStage += deltaSeconds * growthMultiplier;

    int lastStage = plantType.getStageCount() - 1;

    if (withering) {
      if (timeInStage >= PlantGrowthConfig.WITHER_DISPLAY_SECONDS) {
        removed = true;
      }
      return;
    }

    if (stageIndex < lastStage) {
      if (timeInStage >= PlantGrowthConfig.stageDuration(plantType)) {
        stageIndex++;
        timeInStage = 0;
      }
      return;
    }

    // Fruiting stage — harvest window
    if (timeInStage >= PlantGrowthConfig.fruitingWindow(plantType)) {
      withering = true;
      timeInStage = 0;
    }
  }

  public boolean isReadyToHarvest() {
    return !removed && !withering && stageIndex == plantType.getStageCount() - 1;
  }

  public boolean isRemoved() {
    return removed;
  }

  public boolean isWithering() {
    return withering;
  }

  public void render(GraphicsContext gc, double screenX, double screenY, double scale) {
    if (plantBasesSheet == null || removed) {
      return;
    }

    int cell = PlantBasesAtlas.SOURCE_CELL_SIZE;
    int sourceX = PlantBasesAtlas.getPlantX(plantType, stageIndex);
    int sourceY = PlantBasesAtlas.getPlantY(plantType);
    double destWidth = cell * scale;
    double destHeight = cell * scale;

    gc.setImageSmoothing(false);
    if (withering) {
      gc.setGlobalAlpha(0.55);
    }
    gc.drawImage(
        plantBasesSheet, sourceX, sourceY, cell, cell, screenX, screenY, destWidth, destHeight);
    gc.setGlobalAlpha(1.0);
    gc.setImageSmoothing(true);
  }

  public Rectangle2D getViewport() {
    int cell = PlantBasesAtlas.SOURCE_CELL_SIZE;
    int sourceX = PlantBasesAtlas.getPlantX(plantType, stageIndex);
    int sourceY = PlantBasesAtlas.getPlantY(plantType);
    return new Rectangle2D(sourceX, sourceY, cell, cell);
  }

  public PlantType getPlantType() {
    return plantType;
  }

  public int getStageIndex() {
    return stageIndex;
  }

  public int getTileX() {
    return tileX;
  }

  public int getTileY() {
    return tileY;
  }

  public void setStageIndex(int stageIndex) {
    this.stageIndex = stageIndex;
  }

  /** Debug: advance one growth stage. */
  public void growNextStage() {
    if (removed || withering) {
      return;
    }
    int lastStage = plantType.getStageCount() - 1;
    if (stageIndex < lastStage) {
      stageIndex++;
      timeInStage = 0;
    }
  }
}
