package ua.notion.presentation.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ua.notion.domain.entity.User;

public class MenuController {

  @FXML private Label welcomeLabel;

  @FXML private Button playButton;

  @FXML private Button gardenButton;

  @FXML private Button shopButton;

  @FXML private Button achievementsButton;

  @FXML private Button tasksButton;

  @FXML private Button settingsButton;

  @FXML private Button logoutButton;

  private User currentUser;

  public void setCurrentUser(User user) {
    this.currentUser = user;
    if (user != null) {
      welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
    }
  }

  @FXML
  private void handlePlay() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game.fxml"));
      Parent root = loader.load();

      Stage stage = (Stage) playButton.getScene().getWindow();
      Scene scene = new Scene(root, 1024, 768);
      stage.setScene(scene);
      stage.setTitle("Garden Simulator - Game");
      stage.setMinWidth(1024);
      stage.setMinHeight(768);
      stage.setMaxWidth(1920);
      stage.setMaxHeight(1080);
    } catch (IOException e) {
      showError("Error", "Failed to start game: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @FXML
  private void handleGarden() {
    try {
      com.google.inject.Injector injector =
          com.google.inject.Guice.createInjector(
              new ua.notion.infrastructure.config.PersistenceModule(),
              new ua.notion.infrastructure.config.ServiceModule());

      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gardens.fxml"));

      ua.notion.presentation.viewmodel.GardenManagementViewModel viewModel =
          injector.getInstance(ua.notion.presentation.viewmodel.GardenManagementViewModel.class);
      ua.notion.presentation.controller.GardenManagementController controller =
          new ua.notion.presentation.controller.GardenManagementController(viewModel);

      loader.setController(controller);
      Parent root = loader.load();

      controller.setCurrentUser(currentUser);

      Stage stage = (Stage) gardenButton.getScene().getWindow();
      Scene scene = new Scene(root, 800, 600);
      stage.setScene(scene);
      stage.setTitle("Garden Simulator - My Gardens");
    } catch (IOException e) {
      showError("Error", "Failed to open gardens: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @FXML
  private void handleShop() {
    try {
      com.google.inject.Injector injector =
          com.google.inject.Guice.createInjector(
              new ua.notion.infrastructure.config.PersistenceModule(),
              new ua.notion.infrastructure.config.ServiceModule());

      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/plants.fxml"));

      ua.notion.presentation.viewmodel.PlantCatalogViewModel viewModel =
          injector.getInstance(ua.notion.presentation.viewmodel.PlantCatalogViewModel.class);
      ua.notion.presentation.controller.PlantCatalogController controller =
          new ua.notion.presentation.controller.PlantCatalogController(viewModel);

      loader.setController(controller);
      Parent root = loader.load();

      controller.setCurrentUser(currentUser);

      Stage stage = (Stage) shopButton.getScene().getWindow();
      Scene scene = new Scene(root, 800, 600);
      stage.setScene(scene);
      stage.setTitle("Garden Simulator - Plant Catalog");
    } catch (IOException e) {
      showError("Error", "Failed to open plant catalog: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @FXML
  private void handleAchievements() {
    try {
      com.google.inject.Injector injector =
          com.google.inject.Guice.createInjector(
              new ua.notion.infrastructure.config.PersistenceModule(),
              new ua.notion.infrastructure.config.ServiceModule());

      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/achievements.fxml"));

      ua.notion.presentation.viewmodel.AchievementViewModel viewModel =
          injector.getInstance(ua.notion.presentation.viewmodel.AchievementViewModel.class);
      ua.notion.presentation.controller.AchievementController controller =
          new ua.notion.presentation.controller.AchievementController(viewModel);

      loader.setController(controller);
      Parent root = loader.load();

      controller.setCurrentUser(currentUser);

      Stage stage = (Stage) achievementsButton.getScene().getWindow();
      Scene scene = new Scene(root, 800, 600);
      stage.setScene(scene);
      stage.setTitle("Garden Simulator - Achievements");
    } catch (IOException e) {
      showError("Error", "Failed to open achievements: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @FXML
  private void handleTasks() {
    try {
      com.google.inject.Injector injector =
          com.google.inject.Guice.createInjector(
              new ua.notion.infrastructure.config.PersistenceModule(),
              new ua.notion.infrastructure.config.ServiceModule());

      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/tasks.fxml"));

      ua.notion.presentation.viewmodel.TaskManagementViewModel viewModel =
          injector.getInstance(ua.notion.presentation.viewmodel.TaskManagementViewModel.class);
      ua.notion.presentation.controller.TaskManagementController controller =
          new ua.notion.presentation.controller.TaskManagementController(viewModel);

      loader.setController(controller);
      Parent root = loader.load();

      controller.setCurrentUser(currentUser);

      Stage stage = (Stage) tasksButton.getScene().getWindow();
      Scene scene = new Scene(root, 800, 600);
      stage.setScene(scene);
      stage.setTitle("Garden Simulator - Tasks");
    } catch (IOException e) {
      showError("Error", "Failed to open tasks: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @FXML
  private void handleSettings() {
    showInfo("Settings", "Opening settings...");
  }

  @FXML
  private void handleLogout() {
    try {
      // Get injector to create proper controller with dependencies
      com.google.inject.Injector injector =
          com.google.inject.Guice.createInjector(
              new ua.notion.infrastructure.config.PersistenceModule(),
              new ua.notion.infrastructure.config.ServiceModule());

      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));

      ua.notion.domain.service.auth.AuthenticationService authService =
          injector.getInstance(ua.notion.domain.service.auth.AuthenticationService.class);
      ua.notion.presentation.controller.auth.LoginController loginController =
          new ua.notion.presentation.controller.auth.LoginController(authService);

      Stage stage = (Stage) logoutButton.getScene().getWindow();
      loginController.setStage(stage);

      loader.setController(loginController);
      Parent root = loader.load();

      Scene scene = new Scene(root, 800, 700);
      scene.getStylesheets().add(getClass().getResource("/css/auth.css").toExternalForm());
      stage.setScene(scene);
      stage.setTitle("Garden Simulator - Login");
    } catch (IOException e) {
      showError("Error", "Failed to return to login screen: " + e.getMessage());
      e.printStackTrace();
    }
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
