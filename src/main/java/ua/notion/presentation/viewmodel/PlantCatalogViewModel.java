package ua.notion.presentation.viewmodel;

import com.google.inject.Inject;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ua.notion.domain.entity.Plant;
import ua.notion.domain.service.PlantService;

public class PlantCatalogViewModel {

  private static final System.Logger logger =
      System.getLogger(PlantCatalogViewModel.class.getName());

  private final PlantService plantService;
  private final ObservableList<Plant> plants = FXCollections.observableArrayList();
  private final StringProperty searchQuery = new SimpleStringProperty("");
  private final StringProperty climateFilter = new SimpleStringProperty("All Climates");
  private final StringProperty errorMessage = new SimpleStringProperty();
  private final StringProperty successMessage = new SimpleStringProperty();

  @Inject
  public PlantCatalogViewModel(PlantService plantService) {
    this.plantService = plantService;
    logger.log(System.Logger.Level.INFO, "PlantCatalogViewModel initialized");
  }

  public void loadPlants() {
    try {
      logger.log(System.Logger.Level.INFO, "Loading all plants...");
      List<Plant> allPlants = plantService.getAllPlants();
      logger.log(System.Logger.Level.INFO, "Loaded {0} plants", allPlants.size());
      plants.setAll(allPlants);
      clearMessages();
    } catch (Exception e) {
      logger.log(System.Logger.Level.ERROR, "Failed to load plants", e);
      errorMessage.set("Failed to load plants: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void applyFilters() {
    try {
      String query = searchQuery.get().trim().toLowerCase();
      String climate = climateFilter.get();
      logger.log(
          System.Logger.Level.INFO, "Applying filters: query={0}, climate={1}", query, climate);

      List<Plant> filtered = plantService.getAllPlants();

      if (!"All Climates".equals(climate)) {
        filtered = filtered.stream().filter(p -> p.getClimateType().equals(climate)).toList();
      }

      if (!query.isEmpty()) {
        filtered =
            filtered.stream()
                .filter(
                    p ->
                        p.getName().toLowerCase().contains(query)
                            || p.getSpecies().toLowerCase().contains(query))
                .toList();
      }

      logger.log(System.Logger.Level.INFO, "Found {0} matching plants", filtered.size());
      plants.setAll(filtered);
      clearMessages();
    } catch (Exception e) {
      logger.log(System.Logger.Level.ERROR, "Failed to apply filters", e);
      errorMessage.set("Failed to apply filters: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void clearFilters() {
    searchQuery.set("");
    climateFilter.set("All Climates");
    loadPlants();
  }

  public void createPlant(
      String name, String species, Integer growthDays, String climateType, String iconKey) {
    try {
      logger.log(
          System.Logger.Level.INFO,
          "Creating plant: name={0}, species={1}, growthDays={2}, climate={3}, icon={4}",
          name,
          species,
          growthDays,
          climateType,
          iconKey);
      Plant plant = plantService.createPlant(name, species, growthDays, climateType, iconKey);
      logger.log(System.Logger.Level.INFO, "Plant created with ID: {0}", plant.getId());
      successMessage.set("Plant created successfully!");
      loadPlants();
    } catch (Exception e) {
      logger.log(System.Logger.Level.ERROR, "Failed to create plant", e);
      errorMessage.set("Failed to create plant: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void updatePlant(Plant plant) {
    try {
      logger.log(System.Logger.Level.INFO, "Updating plant ID: {0}", plant.getId());
      plantService.updatePlant(plant);
      logger.log(System.Logger.Level.INFO, "Plant updated successfully");
      successMessage.set("Plant updated successfully!");
      loadPlants();
    } catch (Exception e) {
      logger.log(System.Logger.Level.ERROR, "Failed to update plant", e);
      errorMessage.set("Failed to update plant: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void deletePlant(Integer plantId) {
    try {
      logger.log(System.Logger.Level.INFO, "Deleting plant ID: {0}", plantId);
      plantService.deletePlant(plantId);
      logger.log(System.Logger.Level.INFO, "Plant deleted successfully");
      successMessage.set("Plant deleted successfully!");
      loadPlants();
    } catch (Exception e) {
      logger.log(System.Logger.Level.ERROR, "Failed to delete plant", e);
      errorMessage.set("Failed to delete plant: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void clearMessages() {
    errorMessage.set(null);
    successMessage.set(null);
  }

  // Getters for properties
  public ObservableList<Plant> getPlants() {
    return plants;
  }

  public StringProperty searchQueryProperty() {
    return searchQuery;
  }

  public StringProperty climateFilterProperty() {
    return climateFilter;
  }

  public StringProperty errorMessageProperty() {
    return errorMessage;
  }

  public StringProperty successMessageProperty() {
    return successMessage;
  }
}
