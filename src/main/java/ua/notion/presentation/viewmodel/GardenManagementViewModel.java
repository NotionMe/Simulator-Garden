package ua.notion.presentation.viewmodel;

import com.google.inject.Inject;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ua.notion.domain.entity.Garden;
import ua.notion.domain.service.GardenService;

public class GardenManagementViewModel {

  private static final System.Logger logger =
      System.getLogger(GardenManagementViewModel.class.getName());

  private final GardenService gardenService;
  private final ObservableList<Garden> gardens = FXCollections.observableArrayList();
  private final StringProperty searchQuery = new SimpleStringProperty("");
  private final StringProperty errorMessage = new SimpleStringProperty();
  private final StringProperty successMessage = new SimpleStringProperty();
  private Integer currentUserId;

  @Inject
  public GardenManagementViewModel(GardenService gardenService) {
    this.gardenService = gardenService;
    logger.log(System.Logger.Level.INFO, "GardenManagementViewModel initialized");
  }

  public void setCurrentUserId(Integer userId) {
    logger.log(System.Logger.Level.INFO, "Setting current user ID: {0}", userId);
    this.currentUserId = userId;
    loadGardens();
  }

  public void loadGardens() {
    try {
      if (currentUserId != null) {
        logger.log(System.Logger.Level.INFO, "Loading gardens for user ID: {0}", currentUserId);
        List<Garden> userGardens = gardenService.findGardensByUserId(currentUserId);
        logger.log(System.Logger.Level.INFO, "Loaded {0} gardens", userGardens.size());
        gardens.setAll(userGardens);
        clearMessages();
      } else {
        logger.log(System.Logger.Level.WARNING, "Cannot load gardens: currentUserId is null");
      }
    } catch (Exception e) {
      logger.log(System.Logger.Level.ERROR, "Failed to load gardens", e);
      errorMessage.set("Failed to load gardens: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void searchGardens() {
    try {
      String query = searchQuery.get().trim().toLowerCase();
      logger.log(System.Logger.Level.INFO, "Searching gardens with query: {0}", query);
      if (query.isEmpty()) {
        loadGardens();
        return;
      }

      List<Garden> userGardens = gardenService.findGardensByUserId(currentUserId);
      List<Garden> filtered =
          userGardens.stream().filter(g -> g.getName().toLowerCase().contains(query)).toList();
      logger.log(System.Logger.Level.INFO, "Found {0} matching gardens", filtered.size());
      gardens.setAll(filtered);
      clearMessages();
    } catch (Exception e) {
      logger.log(System.Logger.Level.ERROR, "Failed to search gardens", e);
      errorMessage.set("Failed to search gardens: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void clearSearch() {
    searchQuery.set("");
    loadGardens();
  }

  public void createGarden(String name, Integer widthCells, Integer heightCells) {
    try {
      logger.log(
          System.Logger.Level.INFO,
          "Creating garden: userId={0}, name={1}, width={2}, height={3}",
          currentUserId,
          name,
          widthCells,
          heightCells);
      Garden garden = gardenService.createGarden(currentUserId, name, widthCells, heightCells);
      logger.log(System.Logger.Level.INFO, "Garden created with ID: {0}", garden.getId());
      successMessage.set("Garden created successfully!");
      loadGardens();
    } catch (Exception e) {
      logger.log(System.Logger.Level.ERROR, "Failed to create garden", e);
      errorMessage.set("Failed to create garden: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void updateGarden(Garden garden) {
    try {
      logger.log(System.Logger.Level.INFO, "Updating garden ID: {0}", garden.getId());
      gardenService.updateGarden(garden);
      logger.log(System.Logger.Level.INFO, "Garden updated successfully");
      successMessage.set("Garden updated successfully!");
      loadGardens();
    } catch (Exception e) {
      logger.log(System.Logger.Level.ERROR, "Failed to update garden", e);
      errorMessage.set("Failed to update garden: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void deleteGarden(Integer gardenId) {
    try {
      logger.log(System.Logger.Level.INFO, "Deleting garden ID: {0}", gardenId);
      gardenService.deleteGarden(gardenId);
      logger.log(System.Logger.Level.INFO, "Garden deleted successfully");
      successMessage.set("Garden deleted successfully!");
      loadGardens();
    } catch (Exception e) {
      logger.log(System.Logger.Level.ERROR, "Failed to delete garden", e);
      errorMessage.set("Failed to delete garden: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void clearMessages() {
    errorMessage.set(null);
    successMessage.set(null);
  }

  // Getters for properties
  public ObservableList<Garden> getGardens() {
    return gardens;
  }

  public StringProperty searchQueryProperty() {
    return searchQuery;
  }

  public StringProperty errorMessageProperty() {
    return errorMessage;
  }

  public StringProperty successMessageProperty() {
    return successMessage;
  }
}
