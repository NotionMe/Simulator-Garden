package ua.notion.presentation.game;

import java.util.ArrayList;
import java.util.List;
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
    setupResponsiveCanvas();
  }

  private void setupResponsiveCanvas() {
    // Make canvas responsive to parent container size
    canvas.widthProperty().addListener((obs, oldVal, newVal) -> render());
    canvas.heightProperty().addListener((obs, oldVal, newVal) -> render());
  }

  private void setupInputHandlers() {
    canvas.setFocusTraversable(true);
    canvas.setOnKeyPressed(viewModel.getKeyboardHandler()::handleKeyPressed);
    canvas.setOnKeyReleased(viewModel.getKeyboardHandler()::handleKeyReleased);
    canvas.setOnMouseClicked(this::handleMouseClick);
  }

  private void handleMouseClick(javafx.scene.input.MouseEvent event) {
    if (isLoading || !viewModel.getTileMap().isLoaded()) {
      return;
    }

    double mouseX = event.getX();
    double mouseY = event.getY();

    // Convert screen coordinates to tile coordinates
    int[] tileCoords = screenToTile(mouseX, mouseY);
    if (tileCoords != null) {
      int col = tileCoords[0];
      int row = tileCoords[1];

      System.out.println(
          "Clicked at screen (" + mouseX + ", " + mouseY + ") -> tile (" + col + ", " + row + ")");

      // Check if clicked on dirt tile (ID 10)
      if (isValidTile(col, row)) {
        int tileId = getTileId(col, row);
        System.out.println("Tile ID: " + tileId);
        if (tileId == 10) {
          showSeedPlantingModal(col, row);
        }
      }
    }
  }

  private double getCameraOffsetX() {
    return canvas.getWidth() / 2.0 - (viewModel.getPlayer().getX() + 24.0);
  }

  private double getCameraOffsetY() {
    return canvas.getHeight() / 2.0 - (viewModel.getPlayer().getY() + 24.0);
  }

  private int[] screenToTile(double screenX, double screenY) {
    double adjustedX = screenX - getCameraOffsetX();
    double adjustedY = screenY - getCameraOffsetY();

    var orthoCoords = viewModel.getTileMap().getOrthoCoords();
    int col = orthoCoords.toGridCol(adjustedX, adjustedY);
    int row = orthoCoords.toGridRow(adjustedX, adjustedY);

    return new int[] {col, row};
  }

  private boolean isValidTile(int col, int row) {
    return col >= 0
        && col < viewModel.getTileMap().getWidth()
        && row >= 0
        && row < viewModel.getTileMap().getHeight();
  }

  private int getTileId(int col, int row) {
    return viewModel.getTileMap().getTileId(col, row);
  }

  private void showSeedPlantingModal(int col, int row) {
    // Pause game loop while modal is open to prevent flickering
    if (gameLoop != null) {
      gameLoop.stop();
    }

    var modal =
        ua.notion.presentation.controller.SeedPlantingModalController.createModal(
            viewModel.getSeedInventory(),
            plantType -> {
              // Plant the seed at the clicked location
              if (viewModel.getSeedInventory().consumeSeed(plantType)) {
                viewModel.getPlantManager().plantSeed(plantType, col, row);
                System.out.println("Planted " + plantType + " at (" + col + ", " + row + ")");
              }
            },
            () -> {
              // Resume game loop when modal closes
              if (gameLoop != null) {
                gameLoop.start();
              }
            });

    if (modal != null && canvas.getParent() instanceof javafx.scene.layout.StackPane) {
      ((javafx.scene.layout.StackPane) canvas.getParent()).getChildren().add(modal);
    }
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
                    viewModel
                        .getTileMap()
                        .addTestPlants(
                            viewModel.getPlantManager(),
                            ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType.TOMATO);
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
    double width = canvas.getWidth();
    double height = canvas.getHeight();
    gc.setFill(Color.BLACK);
    gc.fillRect(0, 0, width, height);
    gc.setFill(Color.WHITE);
    gc.fillText("Loading...", width / 2.0 - 30, height / 2.0);
  }

  private void renderBackground() {
    double width = canvas.getWidth();
    double height = canvas.getHeight();
    if (backgroundImage != null) {
      gc.drawImage(backgroundImage, 0, 0, width, height);
    } else {
      gc.setFill(Color.BLACK);
      gc.fillRect(0, 0, width, height);
    }
  }

  private void renderGame() {
    double offsetX = getCameraOffsetX();
    double offsetY = getCameraOffsetY();

    viewModel.getTileMap().render(gc, offsetX, offsetY);

    class DepthRenderable {
      final double y;
      final Runnable renderAction;

      DepthRenderable(double y, Runnable renderAction) {
        this.y = y;
        this.renderAction = renderAction;
      }
    }

    List<DepthRenderable> renderQueue = new ArrayList<>();

    // Add plants to the queue
    for (var plant : viewModel.getPlantManager().getAllPlants()) {
      double plantY = plant.getTileY() * 48.0 + 48.0;
      renderQueue.add(
          new DepthRenderable(
              plantY,
              () -> {
                double screenX =
                    viewModel
                            .getTileMap()
                            .getOrthoCoords()
                            .toScreenX(plant.getTileX(), plant.getTileY())
                        + offsetX;
                double screenY =
                    viewModel
                            .getTileMap()
                            .getOrthoCoords()
                            .toScreenY(plant.getTileX(), plant.getTileY())
                        + offsetY;
                double scale = 4.0;
                double destWidth =
                    ua.notion.presentation.game.assets.PlantBasesAtlas.TILE_WIDTH * scale;
                double destHeight =
                    ua.notion.presentation.game.assets.PlantBasesAtlas.TILE_HEIGHT * scale;
                plant.render(
                    gc, screenX + 24.0 - destWidth / 2.0, screenY + 48.0 - destHeight, scale);
              }));
    }

    // Add player to the queue
    double playerBaseY = viewModel.getPlayer().getY() + 48.0;
    renderQueue.add(
        new DepthRenderable(
            playerBaseY,
            () -> {
              viewModel.getPlayer().render(gc, offsetX, offsetY);
            }));

    // Sort queue by vertical Y coordinate
    renderQueue.sort((r1, r2) -> Double.compare(r1.y, r2.y));

    // Render in sorted order
    for (DepthRenderable renderable : renderQueue) {
      renderable.renderAction.run();
    }
  }

  private void renderUI() {
    gc.setFill(Color.WHITE);
    gc.setStroke(Color.BLACK);
    gc.setLineWidth(2);
    gc.strokeText("FPS: " + gameLoop.getFps(), 10, 20);
    gc.fillText("FPS: " + gameLoop.getFps(), 10, 20);
    gc.fillText("WASD to move, G to grow plants", 10, 40);
  }

  public Canvas getCanvas() {
    return canvas;
  }
}
