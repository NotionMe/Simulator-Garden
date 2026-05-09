package ua.notion.presentation.viewmodel;

import com.google.inject.Inject;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ua.notion.domain.entity.Achievement;
import ua.notion.domain.service.AchievementService;

public class AchievementViewModel {

  private static final System.Logger logger =
      System.getLogger(AchievementViewModel.class.getName());

  private final AchievementService achievementService;
  private final ObservableList<Achievement> achievements = FXCollections.observableArrayList();
  private final StringProperty searchQuery = new SimpleStringProperty("");
  private final StringProperty errorMessage = new SimpleStringProperty();
  private Integer currentUserId;

  @Inject
  public AchievementViewModel(AchievementService achievementService) {
    this.achievementService = achievementService;
    logger.log(System.Logger.Level.INFO, "AchievementViewModel initialized");
  }

  public void setCurrentUserId(Integer userId) {
    logger.log(System.Logger.Level.INFO, "Setting current user ID: {0}", userId);
    this.currentUserId = userId;
    loadAchievements();
  }

  public void loadAchievements() {
    try {
      if (currentUserId != null) {
        logger.log(
            System.Logger.Level.INFO, "Loading achievements for user ID: {0}", currentUserId);
        List<Achievement> userAchievements =
            achievementService.findAchievementsByUserId(currentUserId);
        logger.log(System.Logger.Level.INFO, "Loaded {0} achievements", userAchievements.size());
        achievements.setAll(userAchievements);
        clearMessages();
      } else {
        logger.log(System.Logger.Level.WARNING, "Cannot load achievements: currentUserId is null");
      }
    } catch (Exception e) {
      logger.log(System.Logger.Level.ERROR, "Failed to load achievements", e);
      errorMessage.set("Failed to load achievements: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void searchAchievements() {
    try {
      String query = searchQuery.get().trim().toLowerCase();
      logger.log(System.Logger.Level.INFO, "Searching achievements with query: {0}", query);
      if (query.isEmpty()) {
        loadAchievements();
        return;
      }

      List<Achievement> userAchievements =
          achievementService.findAchievementsByUserId(currentUserId);
      List<Achievement> filtered =
          userAchievements.stream()
              .filter(a -> a.getTitle().toLowerCase().contains(query))
              .toList();
      logger.log(System.Logger.Level.INFO, "Found {0} matching achievements", filtered.size());
      achievements.setAll(filtered);
      clearMessages();
    } catch (Exception e) {
      logger.log(System.Logger.Level.ERROR, "Failed to search achievements", e);
      errorMessage.set("Failed to search achievements: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void clearSearch() {
    searchQuery.set("");
    loadAchievements();
  }

  private void clearMessages() {
    errorMessage.set(null);
  }

  // Getters for properties
  public ObservableList<Achievement> getAchievements() {
    return achievements;
  }

  public StringProperty searchQueryProperty() {
    return searchQuery;
  }

  public StringProperty errorMessageProperty() {
    return errorMessage;
  }
}
