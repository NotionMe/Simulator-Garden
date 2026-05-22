package ua.notion.presentation.controller;

import com.google.inject.Inject;
import java.io.IOException;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import ua.notion.domain.entity.Plant;
import ua.notion.domain.entity.User;
import ua.notion.presentation.viewmodel.PlantCatalogViewModel;

public class PlantCatalogController {

  private static final System.Logger logger =
      System.getLogger(PlantCatalogController.class.getName());

  @FXML private Button backButton;
  @FXML private TextField searchField;
  @FXML private ComboBox<String> climateFilterCombo;
  @FXML private Button searchButton;
  @FXML private Button clearFilterButton;
  @FXML private Button addPlantButton;
  @FXML private TableView<Plant> plantTable;
  @FXML private TableColumn<Plant, String> idColumn;
  @FXML private TableColumn<Plant, String> nameColumn;
  @FXML private TableColumn<Plant, String> speciesColumn;
  @FXML private TableColumn<Plant, String> growthDaysColumn;
  @FXML private TableColumn<Plant, String> climateColumn;
  @FXML private TableColumn<Plant, Void> actionsColumn;

  private User currentUser;
  private PlantCatalogViewModel viewModel;

  @Inject
  public PlantCatalogController(PlantCatalogViewModel viewModel) {
    this.viewModel = viewModel;
    logger.log(System.Logger.Level.INFO, "PlantCatalogController initialized");
  }

  public void initialize() {
    logger.log(System.Logger.Level.INFO, "Initializing PlantCatalogController UI");
    plantTable.setItems(viewModel.getPlants());

    setupTableColumns();
    setupFilters();
    setupBindings();
    viewModel.loadPlants();
  }

  private void setupBindings() {
    searchField.textProperty().bindBidirectional(viewModel.searchQueryProperty());
    climateFilterCombo.valueProperty().bindBidirectional(viewModel.climateFilterProperty());

    viewModel
        .errorMessageProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              if (newVal != null && !newVal.isEmpty()) {
                showError("Error", newVal);
              }
            });

    viewModel
        .successMessageProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              if (newVal != null && !newVal.isEmpty()) {
                showInfo("Success", newVal);
              }
            });
  }

  private void setupTableColumns() {
    idColumn.setCellValueFactory(
        data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));

    nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

    speciesColumn.setCellValueFactory(
        data -> new SimpleStringProperty(data.getValue().getSpecies()));

    growthDaysColumn.setCellValueFactory(
        data -> new SimpleStringProperty(String.valueOf(data.getValue().getGrowthDays())));

    climateColumn.setCellValueFactory(
        data -> new SimpleStringProperty(data.getValue().getClimateType()));

    actionsColumn.setCellFactory(
        param ->
            new TableCell<>() {
              private final Button editButton = new Button("Edit");
              private final Button deleteButton = new Button("Delete");
              private final HBox container = new HBox(5, editButton, deleteButton);

              {
                container.setAlignment(Pos.CENTER);
                editButton.getStyleClass().add("secondary-button");
                deleteButton.getStyleClass().add("danger-button");

                editButton.setOnAction(
                    event -> {
                      Plant plant = getTableView().getItems().get(getIndex());
                      handleEditPlant(plant);
                    });

                deleteButton.setOnAction(
                    event -> {
                      Plant plant = getTableView().getItems().get(getIndex());
                      handleDeletePlant(plant);
                    });
              }

              @Override
              protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
              }
            });
  }

  private void setupFilters() {
    climateFilterCombo.setItems(
        javafx.collections.FXCollections.observableArrayList(
            "All Climates", "tropical", "temperate", "arid"));
    climateFilterCombo.setValue("All Climates");

    searchButton.setOnAction(event -> viewModel.applyFilters());
    climateFilterCombo.setOnAction(event -> viewModel.applyFilters());
  }

  @FXML
  private void handleSearch() {
    viewModel.applyFilters();
  }

  @FXML
  private void handleClearFilter() {
    viewModel.clearFilters();
  }

  @FXML
  private void handleAddPlant() {
    logger.log(System.Logger.Level.INFO, "User clicked Add Plant button");
    showPlantDialog(null);
  }

  private void handleEditPlant(Plant plant) {
    logger.log(
        System.Logger.Level.INFO,
        "User clicked Edit Plant button for plant ID: {0}",
        plant.getId());
    showPlantDialog(plant);
  }

  private void handleDeletePlant(Plant plant) {
    logger.log(
        System.Logger.Level.INFO,
        "User clicked Delete Plant button for plant ID: {0}",
        plant.getId());
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Confirm Delete");
    confirm.setHeaderText("Delete Plant");
    confirm.setContentText("Are you sure you want to delete plant '" + plant.getName() + "'?");

    Optional<ButtonType> result = confirm.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      logger.log(
          System.Logger.Level.INFO, "User confirmed deletion of plant ID: {0}", plant.getId());
      viewModel.deletePlant(plant.getId());
    } else {
      logger.log(System.Logger.Level.INFO, "User cancelled deletion");
    }
  }

  private void showPlantDialog(Plant plant) {
    logger.log(
        System.Logger.Level.INFO,
        "Opening plant dialog for: {0}",
        plant == null ? "new plant" : "plant ID " + plant.getId());
    Dialog<Plant> dialog = new Dialog<>();
    dialog.setTitle(plant == null ? "Add Plant" : "Edit Plant");
    dialog.setHeaderText(plant == null ? "Create a new plant" : "Edit plant details");

    ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    TextField nameField = new TextField(plant != null ? plant.getName() : "");
    TextField speciesField = new TextField(plant != null ? plant.getSpecies() : "");
    TextField growthDaysField =
        new TextField(plant != null ? String.valueOf(plant.getGrowthDays()) : "");
    ComboBox<String> climateCombo = new ComboBox<>();
    climateCombo.setItems(
        javafx.collections.FXCollections.observableArrayList("tropical", "temperate", "arid"));
    if (plant != null) climateCombo.setValue(plant.getClimateType());
    TextField iconKeyField = new TextField(plant != null ? plant.getIconKey() : "");

    grid.add(new Label("Name:"), 0, 0);
    grid.add(nameField, 1, 0);
    grid.add(new Label("Species:"), 0, 1);
    grid.add(speciesField, 1, 1);
    grid.add(new Label("Growth Days:"), 0, 2);
    grid.add(growthDaysField, 1, 2);
    grid.add(new Label("Climate Type:"), 0, 3);
    grid.add(climateCombo, 1, 3);
    grid.add(new Label("Icon Key:"), 0, 4);
    grid.add(iconKeyField, 1, 4);

    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(
        dialogButton -> {
          if (dialogButton == saveButtonType) {
            try {
              Plant result = plant != null ? plant : new Plant();
              result.setName(nameField.getText());
              result.setSpecies(speciesField.getText());
              result.setGrowthDays(Integer.parseInt(growthDaysField.getText()));
              result.setClimateType(climateCombo.getValue());
              result.setIconKey(iconKeyField.getText());
              logger.log(
                  System.Logger.Level.INFO,
                  "Plant dialog data: name={0}, species={1}, growthDays={2}, climate={3}, icon={4}",
                  result.getName(),
                  result.getSpecies(),
                  result.getGrowthDays(),
                  result.getClimateType(),
                  result.getIconKey());
              return result;
            } catch (NumberFormatException e) {
              logger.log(System.Logger.Level.ERROR, "Validation failed: Invalid Growth Days", e);
              showError("Error", "Growth Days must be a number");
              return null;
            }
          }
          return null;
        });

    Optional<Plant> result = dialog.showAndWait();
    result.ifPresent(
        plantData -> {
          if (plant == null) {
            logger.log(System.Logger.Level.INFO, "Creating new plant from dialog");
            viewModel.createPlant(
                plantData.getName(),
                plantData.getSpecies(),
                plantData.getGrowthDays(),
                plantData.getClimateType(),
                plantData.getIconKey());
          } else {
            logger.log(System.Logger.Level.INFO, "Updating existing plant from dialog");
            viewModel.updatePlant(plantData);
          }
        });
  }

  @FXML
  private void handleBack() {
    try {
      ua.notion.presentation.ui.SceneCoordinator.forNode(backButton).navigateToMenu(currentUser);
    } catch (IOException e) {
      showError("Error", "Failed to return to menu: " + e.getMessage());
    }
  }

  public void setCurrentUser(User user) {
    this.currentUser = user;
  }

  private void showInfo(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
