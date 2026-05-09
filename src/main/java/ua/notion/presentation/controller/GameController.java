package ua.notion.presentation.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import ua.notion.presentation.game.GameScene;

public class GameController {

  @FXML private StackPane gameContainer;

  private GameScene gameScene;

  @FXML
  public void initialize() {
    System.out.println("GameController initialized");

    gameScene = new GameScene();
    gameScene.initialize();

    // Bind canvas size to container size for responsiveness
    gameScene.getCanvas().widthProperty().bind(gameContainer.widthProperty());
    gameScene.getCanvas().heightProperty().bind(gameContainer.heightProperty());

    gameContainer.getChildren().add(gameScene.getCanvas());

    gameScene.start();
  }

  public void shutdown() {
    if (gameScene != null) {
      gameScene.stop();
    }
  }
}
