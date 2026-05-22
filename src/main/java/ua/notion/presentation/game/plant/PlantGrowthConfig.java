package ua.notion.presentation.game.plant;

import ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType;

public final class PlantGrowthConfig {

  private PlantGrowthConfig() {
    throw new AssertionError("Cannot instantiate utility class");
  }

  /** Seconds per growth stage (until fruiting). */
  public static double stageDuration(PlantType type) {
    return switch (type) {
      case CORN, TOMATO, SUNFLOWER -> 5.0;
      case CARROT, POTATO, BEAN -> 6.0;
      case STRAWBERRY, BLUEBERRY -> 7.0;
      default -> 6.0;
    };
  }

  /** How long the crop stays harvestable before it withers away. */
  public static double fruitingWindow(PlantType type) {
    return switch (type) {
      case CORN, TOMATO -> 12.0;
      case STRAWBERRY, BLUEBERRY -> 10.0;
      default -> 11.0;
    };
  }

  /** Brief wither phase before the plant is removed from the bed. */
  public static final double WITHER_DISPLAY_SECONDS = 1.5;
}
