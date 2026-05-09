package ua.notion.presentation.controller;

import com.google.inject.Inject;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ua.notion.domain.entity.Achievement;
import ua.notion.domain.entity.User;
import ua.notion.presentation.viewmodel.AchievementViewModel;

public class AchievementController {

  @FXML private Button backButton;
  @FXML private TextField searchField;
  @FXML private Button searchButton;
  @FXML private Button clearSearchButton;
  @FXML private TableView<Achievement> achievementTable;
  @FXML private TableColumn<Achievement, String> idColumn;
  @FXML private TableColumn<Achievement, String> titleColumn;
  @FXML private TableColumn<Achievement, String> conditionColumn;
  @FXML private TableColumn<Achievement, String> earnedAtColumn;

  private User currentUser;
  private AchievementViewModel viewModel;
  private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  @Inject
  public AchievementController(AchievementViewModel viewModel) {
    this.viewModel = viewModel;
  }

  public void initialize() {
    achievementTable.setItems(viewModel.getAchievements());

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
  }

  private void setupTableColumns() {
    idColumn.setCellValueFactory(
        data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));

    titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));

    conditionColumn.setCellValueFactory(
        data -> new SimpleStringProperty(data.getValue().getConditionKey()));

    earnedAtColumn.setCellValueFactory(
        data ->
            new SimpleStringProperty(
                data.getValue().getEarnedAt() != null
                    ? data.getValue().getEarnedAt().format(dateFormatter)
                    : "N/A"));
  }

  @FXML
  private void handleSearch() {
    viewModel.searchAchievements();
  }

  @FXML
  private void handleClearSearch() {
    viewModel.clearSearch();
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

  private void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
