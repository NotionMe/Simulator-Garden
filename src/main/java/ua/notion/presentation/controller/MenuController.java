package ua.notion.presentation.controller;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import ua.notion.domain.entity.User;
import ua.notion.domain.service.auth.AuthenticationService;
import ua.notion.infrastructure.config.PersistenceModule;
import ua.notion.infrastructure.config.ServiceModule;
import ua.notion.presentation.controller.auth.LoginController;
import ua.notion.presentation.ui.SceneCoordinator;

public class MenuController {

  @FXML private StackPane menuRoot;
  @FXML private ImageView menuBackground;
  @FXML private Label welcomeLabel;
  @FXML private Button playButton;
  @FXML private Button gardenButton;
  @FXML private Button shopButton;
  @FXML private Button achievementsButton;
  @FXML private Button tasksButton;
  @FXML private Button settingsButton;
  @FXML private Button logoutButton;

  private User currentUser;
  private SceneCoordinator sceneCoordinator;

  @FXML
  private void initialize() {
    Image bg =
        new Image(
            getClass().getResourceAsStream("/assets/UIKIT/Assets/Backgound/Background_Green.png"));
    menuBackground.setImage(bg);
    menuBackground.fitWidthProperty().bind(menuRoot.widthProperty());
    menuBackground.fitHeightProperty().bind(menuRoot.heightProperty());

    menuRoot
        .sceneProperty()
        .addListener(
            (obs, oldScene, newScene) -> {
              if (newScene != null) {
                sceneCoordinator = SceneCoordinator.of((Stage) newScene.getWindow());
              }
            });
    if (menuRoot.getScene() != null) {
      sceneCoordinator = SceneCoordinator.of((Stage) menuRoot.getScene().getWindow());
    }
  }

  public void setCurrentUser(User user) {
    this.currentUser = user;
    if (user != null) {
      welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
    }
  }

  private SceneCoordinator coordinator() {
    if (sceneCoordinator != null) {
      return sceneCoordinator;
    }
    return SceneCoordinator.forNode(menuRoot != null ? menuRoot : playButton);
  }

  private Injector createInjector() {
    return Guice.createInjector(new PersistenceModule(), new ServiceModule());
  }

  @FXML
  private void handlePlay() {
    try {
      coordinator().loadContent("/fxml/game.fxml");
      coordinator().getStage().setTitle("Garden Simulator - Game");
    } catch (Exception e) {
      coordinator().showMessageOverlay("Error", "Failed to start game: " + e.getMessage(), false);
      e.printStackTrace();
    }
  }

  @FXML
  private void handleGarden() {
    navigateWithInjector(
        "/fxml/gardens.fxml",
        "Garden Simulator - My Gardens",
        (loader, injector) -> {
          var viewModel =
              injector.getInstance(
                  ua.notion.presentation.viewmodel.GardenManagementViewModel.class);
          var controller = new GardenManagementController(viewModel);
          loader.setController(controller);
          return controller;
        });
  }

  @FXML
  private void handleShop() {
    navigateWithInjector(
        "/fxml/plants.fxml",
        "Garden Simulator - Plant Catalog",
        (loader, injector) -> {
          var viewModel =
              injector.getInstance(ua.notion.presentation.viewmodel.PlantCatalogViewModel.class);
          var controller = new PlantCatalogController(viewModel);
          loader.setController(controller);
          return controller;
        });
  }

  @FXML
  private void handleAchievements() {
    navigateWithInjector(
        "/fxml/achievements.fxml",
        "Garden Simulator - Achievements",
        (loader, injector) -> {
          var viewModel =
              injector.getInstance(ua.notion.presentation.viewmodel.AchievementViewModel.class);
          var controller = new AchievementController(viewModel);
          loader.setController(controller);
          return controller;
        });
  }

  @FXML
  private void handleTasks() {
    navigateWithInjector(
        "/fxml/tasks.fxml",
        "Garden Simulator - Tasks",
        (loader, injector) -> {
          var viewModel =
              injector.getInstance(ua.notion.presentation.viewmodel.TaskManagementViewModel.class);
          var controller = new TaskManagementController(viewModel);
          loader.setController(controller);
          return controller;
        });
  }

  @FXML
  private void handleSettings() {
    coordinator().showSettingsOverlay();
  }

  @FXML
  private void handleLogout() {
    try {
      coordinator().hideOverlay();
      Injector injector = createInjector();
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
      AuthenticationService authService = injector.getInstance(AuthenticationService.class);
      LoginController loginController = new LoginController(authService);
      loader.setController(loginController);
      Parent root = loader.load();
      coordinator().setContent(root);
      coordinator().addStylesheet("/css/auth.css");
      coordinator().getStage().setTitle("Garden Simulator - Login");
    } catch (Exception e) {
      coordinator().showMessageOverlay("Error", "Failed to logout: " + e.getMessage(), false);
      e.printStackTrace();
    }
  }

  private void navigateWithInjector(String fxml, String title, ControllerFactory factory) {
    SceneCoordinator nav = coordinator();
    try {
      nav.hideOverlay();
      Injector injector = createInjector();
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
      Object controller = factory.create(loader, injector);
      Parent root = loader.load();
      if (controller instanceof GardenManagementController g) {
        g.setCurrentUser(currentUser);
      } else if (controller instanceof PlantCatalogController p) {
        p.setCurrentUser(currentUser);
      } else if (controller instanceof AchievementController a) {
        a.setCurrentUser(currentUser);
      } else if (controller instanceof TaskManagementController t) {
        t.setCurrentUser(currentUser);
      }
      nav.setContent(root);
      nav.addStylesheet("/css/menu.css");
      nav.getStage().setTitle(title);
    } catch (Exception e) {
      nav.showMessageOverlay("Error", e.getMessage(), false);
      e.printStackTrace();
    }
  }

  @FunctionalInterface
  private interface ControllerFactory {
    Object create(FXMLLoader loader, Injector injector);
  }
}
