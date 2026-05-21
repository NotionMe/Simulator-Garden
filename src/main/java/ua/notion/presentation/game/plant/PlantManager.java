package ua.notion.presentation.game.plant;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import ua.notion.presentation.game.assets.PlantBasesAtlas;
import ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType;
import ua.notion.presentation.game.map.OrthogonalCoordinates;

public class PlantManager {
  private final List<PlantSprite> plants;
  private final OrthogonalCoordinates orthoCoords;

  public PlantManager(OrthogonalCoordinates orthoCoords) {
    this.plants = new ArrayList<>();
    this.orthoCoords = orthoCoords;
  }

  public void plantSeed(PlantType plantType, int tileX, int tileY) {
    PlantSprite plant = new PlantSprite(plantType, tileX, tileY);
    plants.add(plant);
  }

  public void growPlant(int tileX, int tileY) {
    plants.stream()
        .filter(p -> p.getTileX() == tileX && p.getTileY() == tileY)
        .forEach(PlantSprite::growNextStage);
  }

  public void growAllPlants() {
    plants.forEach(PlantSprite::growNextStage);
  }

  public boolean harvestPlant(int tileX, int tileY) {
    return plants.removeIf(
        p -> p.getTileX() == tileX && p.getTileY() == tileY && p.isHarvestable());
  }

  public void render(GraphicsContext gc, double offsetX, double offsetY, double scale) {
    for (PlantSprite plant : plants) {
      double screenX = orthoCoords.toScreenX(plant.getTileX(), plant.getTileY()) + offsetX;
      double screenY = orthoCoords.toScreenY(plant.getTileX(), plant.getTileY()) + offsetY;

      double destWidth = PlantBasesAtlas.TILE_WIDTH * scale;
      double destHeight = PlantBasesAtlas.TILE_HEIGHT * scale;

      plant.render(gc, screenX + 24.0 - destWidth / 2.0, screenY + 48.0 - destHeight, scale);
    }
  }

  public PlantSprite getPlantAt(int tileX, int tileY) {
    return plants.stream()
        .filter(p -> p.getTileX() == tileX && p.getTileY() == tileY)
        .findFirst()
        .orElse(null);
  }

  public boolean hasPlantAt(int tileX, int tileY) {
    return getPlantAt(tileX, tileY) != null;
  }

  public List<PlantSprite> getAllPlants() {
    return new ArrayList<>(plants);
  }

  public void clear() {
    plants.clear();
  }
}
