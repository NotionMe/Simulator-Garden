package ua.notion.presentation.game.plant;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import ua.notion.presentation.game.assets.PlantBasesAtlas;
import ua.notion.presentation.game.assets.PlantBasesAtlas.GrowthStage;
import ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType;

public class PlantSprite {
  private static Image plantBasesSheet;

  private final PlantType plantType;
  private GrowthStage currentStage;
  private final int tileX;
  private final int tileY;

  public PlantSprite(PlantType plantType, int tileX, int tileY) {
    this.plantType = plantType;
    this.currentStage = GrowthStage.SEED;
    this.tileX = tileX;
    this.tileY = tileY;
    loadSpriteSheet();
  }

  private static void loadSpriteSheet() {
    if (plantBasesSheet == null) {
      try {
        String path = "/assets/" + PlantBasesAtlas.SPRITE_PATH;
        System.out.println("Loading sprite sheet from: " + path);
        var stream = PlantSprite.class.getResourceAsStream(path);
        if (stream == null) {
          System.err.println("ERROR: Resource stream is null for path: " + path);
          return;
        }
        plantBasesSheet = new Image(stream);
        System.out.println(
            "PlantBases sprite sheet loaded: "
                + plantBasesSheet.getWidth()
                + "x"
                + plantBasesSheet.getHeight()
                + ", error: "
                + plantBasesSheet.isError());
        if (plantBasesSheet.isError()) {
          System.err.println("Image has error flag set!");
          plantBasesSheet.getException().printStackTrace();
        }
      } catch (Exception e) {
        System.err.println("Failed to load PlantBases sprite sheet: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  public void growNextStage() {
    currentStage = currentStage.next();
  }

  public boolean isHarvestable() {
    return currentStage.isHarvestable();
  }

  public void render(GraphicsContext gc, double screenX, double screenY, double scale) {
    if (plantBasesSheet == null) {
      System.err.println("ERROR: plantBasesSheet is null!");
      return;
    }

    int cell = PlantBasesAtlas.SOURCE_CELL_SIZE;
    int sourceX = PlantBasesAtlas.getPlantX(plantType, currentStage);
    int sourceY = PlantBasesAtlas.getPlantY(plantType);
    double destWidth = cell * scale;
    double destHeight = cell * scale;

    gc.setImageSmoothing(false);
    gc.drawImage(
        plantBasesSheet, sourceX, sourceY, cell, cell, screenX, screenY, destWidth, destHeight);
    gc.setImageSmoothing(true);
  }

  public Rectangle2D getViewport() {
    int cell = PlantBasesAtlas.SOURCE_CELL_SIZE;
    int sourceX = PlantBasesAtlas.getPlantX(plantType, currentStage);
    int sourceY = PlantBasesAtlas.getPlantY(plantType);
    return new Rectangle2D(sourceX, sourceY, cell, cell);
  }

  public PlantType getPlantType() {
    return plantType;
  }

  public GrowthStage getCurrentStage() {
    return currentStage;
  }

  public int getTileX() {
    return tileX;
  }

  public int getTileY() {
    return tileY;
  }

  public void setStage(GrowthStage stage) {
    this.currentStage = stage;
  }
}
