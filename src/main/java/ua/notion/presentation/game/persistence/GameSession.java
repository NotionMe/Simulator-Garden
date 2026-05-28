package ua.notion.presentation.game.persistence;

import java.util.HashMap;
import java.util.Map;

public class GameSession {
  private static final Map<Integer, String> plantData = new HashMap<>();

  public static void savePlants(int userId, String data) {
    plantData.put(userId, data);
  }

  public static String loadPlants(int userId) {
    return plantData.getOrDefault(userId, "");
  }

  public static void clear(int userId) {
    plantData.remove(userId);
  }
}
