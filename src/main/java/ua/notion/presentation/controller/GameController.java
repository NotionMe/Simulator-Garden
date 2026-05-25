package ua.notion.presentation.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import ua.notion.domain.entity.User;
import ua.notion.domain.service.PlayerInventoryItemService;
import ua.notion.infrastructure.config.AppInjector;
import ua.notion.presentation.game.GameScene;
import ua.notion.presentation.game.catalog.PlantTypeResolver;
import ua.notion.presentation.ui.SceneCoordinator;

public class GameController {

  @FXML private StackPane gameContainer;
  @FXML private StackPane gameRoot;

  private GameScene gameScene;
  private User currentUser;

  @FXML
  public void initialize() {
    gameContainer.widthProperty().addListener((obs, oldW, newW) -> resizeCanvas());
    gameContainer.heightProperty().addListener((obs, oldH, newH) -> resizeCanvas());
    gameRoot
        .sceneProperty()
        .addListener(
            (obs, oldScene, newScene) -> {
              resizeCanvas();
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

    gameScene = new GameScene(currentUser.getId(), inventoryService, plantTypes);
    gameScene.initialize();

    gameContainer.getChildren().add(gameScene.getCanvas());
    gameScene.getCanvas().setWidth(1);
    gameScene.getCanvas().setHeight(1);

    resizeCanvas();
    gameScene.start();
  }

  private void resizeCanvas() {
    if (gameScene == null || gameContainer == null) {
      return;
    }
    double w = gameContainer.getWidth();
    double h = gameContainer.getHeight();
    if (w <= 0 || h <= 0) {
      return;
    }
    var canvas = gameScene.getCanvas();
    if (Math.abs(canvas.getWidth() - w) > 0.5 || Math.abs(canvas.getHeight() - h) > 0.5) {
      canvas.setWidth(w);
      canvas.setHeight(h);
    }
  }

  public void shutdown() {
    if (gameScene != null) {
      gameScene.stop();
      gameScene = null;
    }
  }
}
