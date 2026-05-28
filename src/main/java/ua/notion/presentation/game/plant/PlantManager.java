package ua.notion.presentation.game.plant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import ua.notion.presentation.game.assets.PlantBasesAtlas;
import ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType;
import ua.notion.presentation.game.map.OrthogonalCoordinates;
import ua.notion.presentation.viewmodel.PlayerInventoryViewModel;

public class PlantManager {
  private final List<PlantSprite> plants;
  private final OrthogonalCoordinates orthoCoords;

  public PlantManager(OrthogonalCoordinates orthoCoords) {
    this.plants = new ArrayList<>();
    this.orthoCoords = orthoCoords;
  }

  public void plantSeed(PlantType plantType, int tileX, int tileY) {
    if (hasPlantAt(tileX, tileY)) {
      return;
    }
    plants.add(new PlantSprite(plantType, tileX, tileY));
  }

  public void update(double deltaSeconds, double growthMultiplier) {
    Iterator<PlantSprite> it = plants.iterator();
    while (it.hasNext()) {
      PlantSprite plant = it.next();
      plant.update(deltaSeconds, growthMultiplier);
      if (plant.isRemoved()) {
        it.remove();
      }
    }
  }

  private int lastHarvestAmount = 0;

  public int getLastHarvestAmount() {
    return lastHarvestAmount;
  }

  public HarvestResult tryHarvest(
      int tileX, int tileY, PlayerInventoryViewModel inventory, boolean isRaining) {
    PlantSprite plant = getPlantAt(tileX, tileY);
    if (plant == null) {
      lastHarvestAmount = 0;
      return HarvestResult.NO_PLANT;
    }
    if (plant.isWithering() || plant.isRemoved()) {
      lastHarvestAmount = 0;
      return HarvestResult.WITHERED_GONE;
    }
    if (!plant.isReadyToHarvest()) {
      lastHarvestAmount = 0;
      return HarvestResult.NOT_READY;
    }

    double roll = Math.random();
    int amount;
    if (roll < (isRaining ? 0.25 : 0.10)) {
      amount = isRaining ? 8 : 4;
    } else if (roll < (isRaining ? 0.60 : 0.35)) {
      amount = isRaining ? 4 : 2;
    } else {
      amount = isRaining ? 2 : 1;
    }
    lastHarvestAmount = amount;

    inventory.addItem(plant.getPlantType(), amount);
    plants.remove(plant);
    return HarvestResult.SUCCESS;
  }

  public void growAllPlants() {
    plants.forEach(PlantSprite::growNextStage);
  }

  public void render(GraphicsContext gc, double offsetX, double offsetY, double scale) {
    for (PlantSprite plant : plants) {
      double screenX = orthoCoords.toScreenX(plant.getTileX(), plant.getTileY()) + offsetX;
      double screenY = orthoCoords.toScreenY(plant.getTileX(), plant.getTileY()) + offsetY;

      double destSize = PlantBasesAtlas.SOURCE_CELL_SIZE * scale;
      plant.render(gc, screenX + 48.0 - destSize / 2.0, screenY + 48.0 - destSize / 2.0, scale);
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

  public String serializePlants() {
    StringBuilder sb = new StringBuilder();
    for (PlantSprite plant : plants) {
      sb.append(plant.getPlantType().name())
          .append(",")
          .append(plant.getTileX())
          .append(",")
          .append(plant.getTileY())
          .append(",")
          .append(plant.getStageIndex())
          .append(";");
    }
    return sb.toString();
  }

  public void deserializePlants(String data) {
    if (data == null || data.isEmpty()) return;
    plants.clear();
    String[] entries = data.split(";");
    for (String entry : entries) {
      if (entry.isEmpty()) continue;
      String[] parts = entry.split(",");
      if (parts.length == 4) {
        try {
          PlantType type = PlantType.valueOf(parts[0]);
          int x = Integer.parseInt(parts[1]);
          int y = Integer.parseInt(parts[2]);
          int stage = Integer.parseInt(parts[3]);
          PlantSprite sprite = new PlantSprite(type, x, y);
          sprite.setStageIndex(stage);
          plants.add(sprite);
        } catch (Exception e) {
          System.err.println("Failed to deserialize plant: " + entry);
        }
      }
    }
  }

  public void clear() {
    plants.clear();
  }
}
