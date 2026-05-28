package ua.notion.presentation.controller;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import ua.notion.domain.entity.User;
import ua.notion.domain.service.AchievementService;
import ua.notion.domain.service.PlayerInventoryItemService;
import ua.notion.infrastructure.config.AppInjector;
import ua.notion.presentation.achievement.AchievementAwarder;
import ua.notion.presentation.game.GameScene;
import ua.notion.presentation.game.catalog.PlantTypeResolver;
import ua.notion.presentation.ui.SceneCoordinator;

public class GameController {

  @FXML private StackPane gameContainer;
  @FXML private StackPane gameRoot;

  private GameScene gameScene;
  private User currentUser;
  private PauseTransition resizeDebounce;

  @FXML
  public void initialize() {
    resizeDebounce = new PauseTransition(Duration.millis(50));
    resizeDebounce.setOnFinished(e -> layoutCanvas());

    gameContainer.widthProperty().addListener((obs, oldW, newW) -> scheduleLayout());
    gameContainer.heightProperty().addListener((obs, oldH, newH) -> scheduleLayout());
    gameRoot
        .sceneProperty()
        .addListener(
            (obs, oldScene, newScene) -> {
              layoutCanvas();
              if (newScene != null) {
                SceneCoordinator.of((Stage) newScene.getWindow())
                    .setOnContentDispose(this::shutdown);
              }
            });
    startGameIfReady();
  }

  public void setCurrentUser(User user) {
    this.currentUser = user;
    startGameIfReady();
  }

  private void startGameIfReady() {
    if (currentUser == null || currentUser.getId() == null || gameScene != null) {
      return;
    }

    var injector = AppInjector.get();
    PlayerInventoryItemService inventoryService =
        injector.getInstance(PlayerInventoryItemService.class);
    PlantTypeResolver plantTypes = injector.getInstance(PlantTypeResolver.class);
    AchievementService achievementService = injector.getInstance(AchievementService.class);
    AchievementAwarder achievements =
        new AchievementAwarder(currentUser.getId(), achievementService);

    gameScene = new GameScene(currentUser.getId(), inventoryService, plantTypes, achievements);
    gameScene.setOnEscapeToMenu(this::returnToMenu);
    gameScene.setOnOpenShop(this::openShop);
    gameScene.initialize();

    var canvas = gameScene.getCanvas();
    gameContainer.getChildren().add(canvas);

    layoutCanvas();
    gameScene.start();
  }

  private void scheduleLayout() {
    resizeDebounce.playFromStart();
  }

  private void layoutCanvas() {
    if (gameScene == null || gameContainer == null) {
      return;
    }
    double width = gameContainer.getWidth();
    double height = gameContainer.getHeight();
    if (width <= 0 || height <= 0) {
      return;
    }

    var canvas = gameScene.getCanvas();
    canvas.setScaleX(1);
    canvas.setScaleY(1);
    canvas.setTranslateX(0);
    canvas.setTranslateY(0);

    if (Math.abs(canvas.getWidth() - width) > 0.5) {
      canvas.setWidth(width);
    }
    if (Math.abs(canvas.getHeight() - height) > 0.5) {
      canvas.setHeight(height);
    }
  }

  public void shutdown() {
    if (gameScene != null) {
      gameScene.stop();
      gameScene = null;
    }
  }

  private void returnToMenu() {
    try {
      shutdown();
      SceneCoordinator.forNode(gameRoot).navigateToMenu(currentUser);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void openShop() {
    try {
      shutdown();
      var injector = AppInjector.get();
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/shop.fxml"));
      var inventoryService = injector.getInstance(PlayerInventoryItemService.class);
      var plantTypes = injector.getInstance(PlantTypeResolver.class);
      var achievementService = injector.getInstance(AchievementService.class);
      var achievements = new AchievementAwarder(currentUser.getId(), achievementService);
      var viewModel =
          new ua.notion.presentation.viewmodel.ShopViewModel(
              inventoryService, plantTypes, achievements);
      var controller = new ShopController(viewModel);
      loader.setController(controller);
      Parent root = loader.load();
      controller.setCurrentUser(currentUser);

      SceneCoordinator coordinator = SceneCoordinator.forNode(gameRoot);
      coordinator.setContent(root);
      coordinator.addStylesheet("/css/menu.css");
      coordinator.getStage().setTitle("Garden Simulator - Shop");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
