package ua.notion.presentation.game.assets;

public final class PlantBasesAtlas {

  private PlantBasesAtlas() {
    throw new AssertionError("Cannot instantiate constants class");
  }

  /** Source grid cell size in Garden_Planters_PlantBases.png (see ReadMe: 16x16). */
  public static final int SOURCE_CELL_SIZE = 16;

  /** Default horizontal step between growth frames (columns 4, 6, 8, …). */
  public static final int DEFAULT_COLUMN_STEP = 2;

  /** Scale when drawing on the 48px map grid (16 * 3 = 48). */
  public static final double RENDER_SCALE = 3.0;

  public static final int COLUMNS = 26;
  public static final int ROWS = 18;

  public static final String SPRITE_PATH = "Garden_Planters/Garden_Planters_PlantBases.png";

  public enum PotLayer {
    BACKGROUND,
    FOREGROUND
  }

  /**
   * Plant rows in the 16px grid (verified against Garden_Planters_PlantBases.png).
   * startRow/startColumn = top-left growth frame; columnStep = spacing to next stage.
   */
  public enum PlantType {
    SUCCULENT(8, 6, 2, 2),
    BEAN(4, 19, 3, 2),
    CORN(4, 4, 3, 2),
    POTATO(5, 4, 3, 2),
    TOMATO(5, 2, 5, 2),
    TURNIP(5, 19, 3, 2),
    SUNFLOWER(12, 4, 3, 2),
    BLUEBERRY(9, 18, 3, 2),
    STRAWBERRY(13, 4, 4, 2),
    CARROT(13, 18, 3, 2);

    private final int startRow;
    private final int startColumn;
    private final int stageCount;
    private final int columnStep;

    PlantType(int startRow, int startColumn, int stageCount, int columnStep) {
      this.startRow = startRow;
      this.startColumn = startColumn;
      this.stageCount = stageCount;
      this.columnStep = columnStep;
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

    public int getColumnStep() {
      return columnStep;
    }
  }

  public enum GrowthStage {
    SEED(0),
    SPROUT(1),
    GROWING(2),
    BLOOMING(3),
    FRUITING(4),
    WITHERED(5);

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

  public static int getPlantX(PlantType type, GrowthStage stage) {
    int stageIndex = Math.min(stage.getOffset(), type.getStageCount() - 1);
    int col = type.getStartColumn() + stageIndex * type.getColumnStep();
    return col * SOURCE_CELL_SIZE;
  }

  public static int getPlantY(PlantType type) {
    return type.getStartRow() * SOURCE_CELL_SIZE;
  }

  public static int getRenderSize() {
    return (int) (SOURCE_CELL_SIZE * RENDER_SCALE);
  }

  public static int getPotX(int potIndex) {
    return potIndex * SOURCE_CELL_SIZE * 4;
  }

  public static int getPotBackgroundY() {
    return 0;
  }

  public static int getPotForegroundY() {
    return SOURCE_CELL_SIZE;
  }
}
