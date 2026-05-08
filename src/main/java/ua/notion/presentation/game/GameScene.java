package ua.notion.presentation.game;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import ua.notion.infrastructure.async.AsyncExecutor;
import ua.notion.presentation.game.viewmodel.GameViewModel;

public class GameScene {
  private final Canvas canvas;
  private final GraphicsContext gc;
  private final GameViewModel viewModel;
  private GameLoop gameLoop;
  private Image backgroundImage;
  private boolean isLoading = true;

  public GameScene() {
    this.canvas = new Canvas(GameConstants.CANVAS_WIDTH, GameConstants.CANVAS_HEIGHT);
    this.gc = canvas.getGraphicsContext2D();
    this.viewModel = new GameViewModel(GameConstants.MAP_WIDTH, GameConstants.MAP_HEIGHT);
    setupInputHandlers();
  }

  private void setupInputHandlers() {
    canvas.setFocusTraversable(true);
    canvas.setOnKeyPressed(viewModel.getKeyboardHandler()::handleKeyPressed);
    canvas.setOnKeyReleased(viewModel.getKeyboardHandler()::handleKeyReleased);
  }

  public void initialize() {
    System.out.println("Initializing game scene...");

    AsyncExecutor.runAsync(
            () -> {
              backgroundImage =
                  new Image(
                      getClass()
                          .getResourceAsStream(
                              "/assets/background/1024x512/Cloudy Sky/Cloudy_Sky-Blue_01-1024x512.png"));
            })
        .thenCompose(v -> viewModel.loadAsync())
        .thenRun(
            () -> {
              AsyncExecutor.runOnUIThread(
                  () -> {
                    isLoading = false;
                    gameLoop = new GameLoop(this);
                    gameLoop.start();
                    canvas.requestFocus();
                    System.out.println("Game scene initialized and started");
                  });
            })
        .exceptionally(
            error -> {
              System.err.println("Failed to initialize game: " + error.getMessage());
              error.printStackTrace();
              return null;
            });
  }

  public void start() {
    System.out.println("Start called - game loop will start after async loading");
  }

  public void stop() {
    if (gameLoop != null) {
      gameLoop.stop();
      System.out.println("Game loop stopped");
    }
  }

  public void update(double deltaTime) {
    if (isLoading) {
      return;
    }
    viewModel.update(deltaTime);
  }

  public void render() {
    if (isLoading) {
      renderLoading();
      return;
    }

    renderBackground();
    renderGame();
    renderUI();
  }

  private void renderLoading() {
    gc.setFill(Color.BLACK);
    gc.fillRect(0, 0, GameConstants.CANVAS_WIDTH, GameConstants.CANVAS_HEIGHT);
    gc.setFill(Color.WHITE);
    gc.fillText(
        "Loading...", GameConstants.CANVAS_WIDTH / 2.0 - 30, GameConstants.CANVAS_HEIGHT / 2.0);
  }

  private void renderBackground() {
    if (backgroundImage != null) {
      gc.drawImage(backgroundImage, 0, 0, GameConstants.CANVAS_WIDTH, GameConstants.CANVAS_HEIGHT);
    } else {
      gc.setFill(Color.BLACK);
      gc.fillRect(0, 0, GameConstants.CANVAS_WIDTH, GameConstants.CANVAS_HEIGHT);
    }
  }

  private void renderGame() {
    viewModel.getTileMap().render(gc);
    viewModel.getPlayer().render(gc, GameConstants.MAP_OFFSET_X, GameConstants.MAP_OFFSET_Y);
  }

  private void renderUI() {
    gc.setFill(Color.WHITE);
    gc.setStroke(Color.BLACK);
    gc.setLineWidth(2);
    gc.strokeText("FPS: " + gameLoop.getFps(), 10, 20);
    gc.fillText("FPS: " + gameLoop.getFps(), 10, 20);
    gc.fillText("WASD to move", 10, 40);
  }

  public Canvas getCanvas() {
    return canvas;
  }
}
