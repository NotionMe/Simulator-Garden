package ua.notion.presentation.game.plant;

public enum HarvestResult {
  /** Crop was ripe and added to harvest inventory. */
  SUCCESS,
  /** Plant is still growing. */
  NOT_READY,
  /** Fruiting window expired; crop is gone or cannot be collected. */
  WITHERED_GONE,
  /** No plant on this bed. */
  NO_PLANT
}
