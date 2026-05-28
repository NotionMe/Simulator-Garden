package ua.notion.presentation.achievement;

import java.util.Optional;
import java.util.function.Consumer;
import ua.notion.domain.entity.Achievement;
import ua.notion.domain.service.AchievementService;
import ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType;

public class AchievementAwarder {

  private final int userId;
  private final AchievementService achievementService;
  private Consumer<String> notificationHandler;

  public AchievementAwarder(int userId, AchievementService achievementService) {
    this.userId = userId;
    this.achievementService = achievementService;
  }

  public void setNotificationHandler(Consumer<String> notificationHandler) {
    this.notificationHandler = notificationHandler;
  }

  public void gameEntered() {
    unlock("First Steps", "game_entered_once");
  }

  public void inventoryOpened() {
    unlock("Packed Bag", "inventory_opened_once");
  }

  public void shopVisited() {
    unlock("Window Shopper", "shop_visited_once");
  }

  public void itemBought(PlantType type) {
    unlock("First Purchase", "shop_bought_once");
    unlock("Bought " + displayName(type), "shop_bought_" + key(type));
  }

  public void itemSold(PlantType type) {
    unlock("First Sale", "shop_sold_once");
    unlock("Sold " + displayName(type), "shop_sold_" + key(type));
  }

  public void seedPlanted(PlantType type) {
    unlock("First Planting", "seed_planted_once");
    unlock("Planted " + displayName(type), "seed_planted_" + key(type));
  }

  public void cropHarvested(PlantType type, int inventoryTotal) {
    unlock("First Harvest", "harvested_once");
    unlock("Harvested " + displayName(type), "harvested_" + key(type));
    if (inventoryTotal >= 5) {
      unlock("Small Stockpile", "inventory_total_5");
    }
    if (inventoryTotal >= 10) {
      unlock("Full Basket", "inventory_total_10");
    }
    if (inventoryTotal >= 25) {
      unlock("Garden Hoarder", "inventory_total_25");
    }
  }

  public void debugGrowthUsed() {
    unlock("Time Bender", "debug_growth_used_once");
  }

  private void unlock(String title, String conditionKey) {
    Optional<Achievement> achievement =
        achievementService.unlockIfMissing(userId, title, conditionKey);
    achievement.ifPresent(value -> notifyUnlocked(value.getTitle()));
  }

  private void notifyUnlocked(String title) {
    if (notificationHandler != null) {
      notificationHandler.accept("Achievement unlocked: " + title);
    }
  }

  private static String key(PlantType type) {
    return type.name().toLowerCase();
  }

  private static String displayName(PlantType type) {
    String value = type.name().toLowerCase();
    return value.substring(0, 1).toUpperCase() + value.substring(1);
  }
}
