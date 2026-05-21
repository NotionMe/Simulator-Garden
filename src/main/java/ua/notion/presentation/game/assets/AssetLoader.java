package ua.notion.presentation.game.assets;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javafx.scene.image.Image;
import ua.notion.infrastructure.async.AsyncExecutor;

public class AssetLoader {
  private final Map<String, Image> imageCache = new HashMap<>();
  private boolean isLoading = false;

  public CompletableFuture<Void> loadAssetsAsync() {
    if (isLoading) {
      return CompletableFuture.completedFuture(null);
    }

    isLoading = true;

    return AsyncExecutor.runAsync(
            () -> {
              System.out.println("Loading game assets...");
              preloadImages();
              System.out.println("Assets loaded successfully");
            })
        .whenComplete((result, error) -> isLoading = false);
  }

  private void preloadImages() {
    loadImage(
        "background", "/assets/background/1024x512/Cloudy Sky/Cloudy_Sky-Blue_01-1024x512.png");
    loadImage("tilemap", "/assets/tilemaps/spring tilemap.png");
    loadImage("player", "/assets/sprites/characters/main character/walk and idle.png");
  }

  private void loadImage(String key, String path) {
    try {
      Image image = new Image(getClass().getResourceAsStream(path));
      imageCache.put(key, image);
    } catch (Exception e) {
      System.err.println("Failed to load image: " + path + " - " + e.getMessage());
    }
  }

  public Image getImage(String key) {
    return imageCache.get(key);
  }

  public boolean hasImage(String key) {
    return imageCache.containsKey(key);
  }

  public void clearCache() {
    imageCache.clear();
  }
}
