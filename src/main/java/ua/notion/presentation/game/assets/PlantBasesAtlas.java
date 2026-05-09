package ua.notion.presentation.game.assets;

public final class PlantBasesAtlas {

  private PlantBasesAtlas() {
    throw new AssertionError("Cannot instantiate constants class");
  }

  public static final int TILE_WIDTH = 64;
  public static final int TILE_HEIGHT = 64;
  public static final int COLUMNS = 10; // всього колонок у sprite sheet (416/16)
  public static final int ROWS = 5; // всього рядків у sprite sheet (288/16)

  public static final String SPRITE_PATH = "Garden_Planters/Garden_Planters_PlantBases.png";

  // Шари горщика (верхні 2 рядки)
  public enum PotLayer {
    BACKGROUND, // задній фон горщика
    FOREGROUND // передня стінка горщика для Z-ordering
  }

  // Типи рослин (рядки 3-7, по 2 рослини в рядку)
  public enum PlantType {
    SUCCULENT(2, 0, 5), // Рядок 3, ліворуч, 5 стадій
    BEAN(2, 13, 6), // Рядок 3, праворуч, 6 стадій
    CORN(4, 0, 6), // Рядок 4, ліворуч, 6 стадій
    POTATO(4, 13, 6), // Рядок 4, праворуч, 6 стадій
    TOMATO(6, 0, 6), // Рядок 5, ліворуч, 6 стадій
    TURNIP(6, 13, 5), // Рядок 5, праворуч, 5 стадій
    SUNFLOWER(8, 0, 5), // Рядок 6, ліворуч, 5 стадій
    BLUEBERRY(8, 13, 6), // Рядок 6, праворуч, 6 стадій
    STRAWBERRY(10, 0, 6), // Рядок 7, ліворуч, 6 стадій
    CARROT(10, 13, 5); // Рядок 7, праворуч, 5 стадій

    private final int startRow;
    private final int startColumn;
    private final int stageCount;

    PlantType(int startRow, int startColumn, int stageCount) {
      this.startRow = startRow;
      this.startColumn = startColumn;
      this.stageCount = stageCount;
    }

    public int getStartRow() {
      return startRow;
    }

    public int getStartColumn() {
      return startColumn;
    }

    public int getStageCount() {
      return stageCount;
    }
  }

  // Стадії росту
  public enum GrowthStage {
    SEED(0), // Насіння
    SPROUT(1), // Паросток
    GROWING(2), // Ріст
    BLOOMING(3), // Цвітіння
    FRUITING(4), // Плодоношення
    WITHERED(5); // Зів'янення (не у всіх)

    private final int offset;

    GrowthStage(int offset) {
      this.offset = offset;
    }

    public int getOffset() {
      return offset;
    }

    public GrowthStage next() {
      int nextOrdinal = ordinal() + 1;
      if (nextOrdinal >= values().length) {
        return this;
      }
      return values()[nextOrdinal];
    }

    public boolean isHarvestable() {
      return this == FRUITING || this == WITHERED;
    }
  }

  // Координати для горщиків (верхні 2 рядки)
  public static int getPotX(int potIndex) {
    return potIndex * TILE_WIDTH;
  }

  public static int getPotBackgroundY() {
    return 0; // перший рядок
  }

  public static int getPotForegroundY() {
    return TILE_HEIGHT; // другий рядок
  }

  // Координати для рослин
  public static int getPlantX(PlantType type, GrowthStage stage) {
    if (stage.getOffset() >= type.getStageCount()) {
      return type.getStartColumn() + (type.getStageCount() - 1) * TILE_WIDTH;
    }
    return type.getStartColumn() * TILE_WIDTH + stage.getOffset() * TILE_WIDTH;
  }

  public static int getPlantY(PlantType type) {
    return type.getStartRow() * TILE_HEIGHT;
  }

  // Старі методи для сумісності
  @Deprecated
  public static int getX(GrowthStage stage) {
    return stage.getOffset() * TILE_WIDTH;
  }

  @Deprecated
  public static int getY(PlantType type) {
    return type.getStartRow() * TILE_HEIGHT;
  }
}
