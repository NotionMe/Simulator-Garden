package ua.notion.presentation.controller;

import com.google.inject.Inject;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import ua.notion.domain.entity.Garden;
import ua.notion.domain.entity.User;
import ua.notion.presentation.viewmodel.GardenManagementViewModel;

public class GardenManagementController {

  @FXML private Button backButton;
  @FXML private TextField searchField;
  @FXML private Button searchButton;
  @FXML private Button clearSearchButton;
  @FXML private Button addGardenButton;
  @FXML private TableView<Garden> gardenTable;
  @FXML private TableColumn<Garden, String> idColumn;
  @FXML private TableColumn<Garden, String> nameColumn;
  @FXML private TableColumn<Garden, String> sizeColumn;
  @FXML private TableColumn<Garden, String> createdAtColumn;
  @FXML private TableColumn<Garden, Void> actionsColumn;

  private User currentUser;
  private GardenManagementViewModel viewModel;
  private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  @Inject
  public GardenManagementController(GardenManagementViewModel viewModel) {
    this.viewModel = viewModel;
  }

  public void initialize() {
    gardenTable.setItems(viewModel.getGardens());

    setupTableColumns();
    setupBindings();
  }

  private void setupBindings() {
    searchField.textProperty().bindBidirectional(viewModel.searchQueryProperty());

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

    sizeColumn.setCellValueFactory(
        data ->
            new SimpleStringProperty(
                data.getValue().getWidthCells() + "×" + data.getValue().getHeightCells()));

    createdAtColumn.setCellValueFactory(
        data ->
            new SimpleStringProperty(
                data.getValue().getCreatedAt() != null
                    ? data.getValue().getCreatedAt().format(dateFormatter)
                    : "N/A"));

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
                      Garden garden = getTableView().getItems().get(getIndex());
                      handleEditGarden(garden);
                    });

                deleteButton.setOnAction(
                    event -> {
                      Garden garden = getTableView().getItems().get(getIndex());
                      handleDeleteGarden(garden);
                    });
              }

              @Override
              protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
              }
            });
  }

  @FXML
  private void handleSearch() {
    viewModel.searchGardens();
  }

  @FXML
  private void handleClearSearch() {
    viewModel.clearSearch();
  }

  @FXML
  private void handleAddGarden() {
    showGardenDialog(null);
  }

  private void handleEditGarden(Garden garden) {
    showGardenDialog(garden);
  }

  private void handleDeleteGarden(Garden garden) {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Confirm Delete");
    confirm.setHeaderText("Delete Garden");
    confirm.setContentText("Are you sure you want to delete garden '" + garden.getName() + "'?");

    Optional<ButtonType> result = confirm.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      viewModel.deleteGarden(garden.getId());
    }
  }

  private void showGardenDialog(Garden garden) {
    Dialog<Garden> dialog = new Dialog<>();
    dialog.setTitle(garden == null ? "Add Garden" : "Edit Garden");
    dialog.setHeaderText(garden == null ? "Create a new garden" : "Edit garden details");

    ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    TextField nameField = new TextField(garden != null ? garden.getName() : "");
    TextField widthField =
        new TextField(garden != null ? String.valueOf(garden.getWidthCells()) : "10");
    TextField heightField =
        new TextField(garden != null ? String.valueOf(garden.getHeightCells()) : "10");

    grid.add(new Label("Garden Name:"), 0, 0);
    grid.add(nameField, 1, 0);
    grid.add(new Label("Width (cells):"), 0, 1);
    grid.add(widthField, 1, 1);
    grid.add(new Label("Height (cells):"), 0, 2);
    grid.add(heightField, 1, 2);

    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(
        dialogButton -> {
          if (dialogButton == saveButtonType) {
            try {
              Garden result = garden != null ? garden : new Garden();
              result.setName(nameField.getText());
              result.setWidthCells(Integer.parseInt(widthField.getText()));
              result.setHeightCells(Integer.parseInt(heightField.getText()));
              if (garden == null) {
                result.setUserId(currentUser.getId());
              }
              return result;
            } catch (NumberFormatException e) {
              showError("Error", "Width and Height must be numbers");
              return null;
            }
          }
          return null;
        });

    Optional<Garden> result = dialog.showAndWait();
    result.ifPresent(
        gardenData -> {
          if (garden == null) {
            viewModel.createGarden(
                gardenData.getName(), gardenData.getWidthCells(), gardenData.getHeightCells());
          } else {
            viewModel.updateGarden(gardenData);
          }
        });
  }

  @FXML
  private void handleBack() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
      Parent root = loader.load();

      MenuController controller = loader.getController();
      controller.setCurrentUser(currentUser);

      Stage stage = (Stage) backButton.getScene().getWindow();
      Scene scene = new Scene(root, 800, 600);
      stage.setScene(scene);
    } catch (IOException e) {
      showError("Error", "Failed to return to menu: " + e.getMessage());
    }
  }

  public void setCurrentUser(User user) {
    this.currentUser = user;
    viewModel.setCurrentUserId(user.getId());
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
