package ua.notion.presentation.game;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import ua.notion.infrastructure.async.AsyncExecutor;
import ua.notion.presentation.controller.PlayerInventoryController;
import ua.notion.presentation.controller.SeedPlantingModalController;
import ua.notion.presentation.game.assets.GardenTileAtlas;
import ua.notion.presentation.game.plant.HarvestResult;
import ua.notion.presentation.game.viewmodel.GameViewModel;

public class GameScene {
  private final Canvas canvas;
  private final GraphicsContext gc;
  private final GameViewModel viewModel;
  private GameLoop gameLoop;
  private Image backgroundImage;
  private boolean isLoading = true;

  private Pane seedPickerOverlay;
  private SeedPlantingModalController seedPickerController;
  private AnimationTimer uiOverlayTimer;
  private int pendingBedCol = -1;
  private int pendingBedRow = -1;
  private int harvestMessageTicks;

  private Pane inventoryOverlay;
  private PlayerInventoryController inventoryController;
  private Runnable onEscapeToMenu;
  private Runnable onOpenShop;

  public GameScene(
      int userId,
      ua.notion.domain.service.PlayerInventoryItemService inventoryService,
      ua.notion.presentation.game.catalog.PlantTypeResolver plantTypes) {
    this.canvas = new Canvas(GameConstants.CANVAS_WIDTH, GameConstants.CANVAS_HEIGHT);
    this.gc = canvas.getGraphicsContext2D();
    this.viewModel =
        new GameViewModel(
            userId,
            inventoryService,
            plantTypes,
            GameConstants.MAP_WIDTH,
            GameConstants.MAP_HEIGHT);
    setupInputHandlers();
  }

  private void setupInputHandlers() {
    canvas.setFocusTraversable(true);
    canvas.setOnKeyPressed(
        event -> {
          viewModel.getKeyboardHandler().handleKeyPressed(event);
          if (event.getCode() == KeyCode.ESCAPE) {
            handleEscape();
          } else if (event.getCode() == KeyCode.I) {
            toggleInventory();
          }
        });
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

      if (isValidTile(col, row)) {
        int tileId = getTileId(col, row);
        System.out.println("Tile ID: " + tileId);

        if (viewModel.getTileMap().isShopTile(col, row)) {
          openShop();
          return;
        }

        if (seedPickerOverlay != null) {
          if (GardenTileAtlas.isGardenBed(tileId)) {
            pendingBedCol = col;
            pendingBedRow = row;
          } else {
            closeSeedPicker();
          }
          return;
        }

        if (GardenTileAtlas.isGardenBed(tileId)) {
          int[] anchor = GardenTileAtlas.bedAnchorForTile(tileId, col, row);
          if (tryHarvestOnBed(anchor[0], anchor[1])) {
            return;
          }
          showSeedPicker(col, row);
        }
      }
    }
  }

  private void handleEscape() {
    if (seedPickerOverlay != null || inventoryOverlay != null) {
      closeSeedPicker();
      closeInventory();
      return;
    }
    if (onEscapeToMenu != null) {
      onEscapeToMenu.run();
    }
  }

  private void openShop() {
    closeSeedPicker();
    closeInventory();
    if (onOpenShop != null) {
      onOpenShop.run();
    }
  }

  private double getCameraOffsetX() {
    double scale = getViewportScale();
    double mapWidth = viewModel.getTileMap().getWidth() * 48.0;
    return (canvas.getWidth() - mapWidth * scale) / (2.0 * scale);
  }

  private double getCameraOffsetY() {
    double scale = getViewportScale();
    double mapHeight = viewModel.getTileMap().getHeight() * 48.0;
    return (canvas.getHeight() - mapHeight * scale) / (2.0 * scale);
  }

  private double getViewportScale() {
    double mapWidth = viewModel.getTileMap().getWidth() * 48.0;
    double mapHeight = viewModel.getTileMap().getHeight() * 48.0;
    double availableWidth = Math.max(1.0, canvas.getWidth());
    double availableHeight = Math.max(1.0, canvas.getHeight());
    return Math.min(
        1.0, Math.max(0.1, Math.min(availableWidth / mapWidth, availableHeight / mapHeight)));
  }

  private int[] screenToTile(double screenX, double screenY) {
    double scale = getViewportScale();
    double adjustedX = screenX / scale - getCameraOffsetX();
    double adjustedY = screenY / scale - getCameraOffsetY();

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

  private boolean tryHarvestOnBed(int anchorCol, int anchorRow) {
    HarvestResult result = viewModel.tryHarvestAt(anchorCol, anchorRow);
    if (result == HarvestResult.NO_PLANT) {
      return false;
    }
    harvestMessageTicks = 180;
    if (result == HarvestResult.SUCCESS && inventoryController != null) {
      inventoryController.refresh();
    }
    System.out.println(viewModel.getLastHarvestMessage());
    return true;
  }

  private void showSeedPicker(int col, int row) {
    closeSeedPicker();
    pendingBedCol = col;
    pendingBedRow = row;

    Pane overlay =
        SeedPlantingModalController.createPicker(
            viewModel.getSeedInventory(),
            plantType -> plantOnPendingBed(plantType),
            this::onSeedPickerClosed,
            controller -> seedPickerController = controller);

    if (overlay == null || !(canvas.getParent() instanceof StackPane stack)) {
      return;
    }

    seedPickerOverlay = overlay;
    bindOverlayToStack(overlay, stack);
    stack.getChildren().add(overlay);
    startUiOverlayTimer();
    updateSeedPickerPosition();
  }

  private void bindOverlayToStack(Pane overlay, StackPane stack) {
    overlay.prefWidthProperty().bind(stack.widthProperty());
    overlay.prefHeightProperty().bind(stack.heightProperty());
    overlay.minWidthProperty().bind(stack.widthProperty());
    overlay.minHeightProperty().bind(stack.heightProperty());
    overlay.maxWidthProperty().bind(stack.widthProperty());
    overlay.maxHeightProperty().bind(stack.heightProperty());
  }

  private void unbindOverlay(Pane overlay) {
    overlay.prefWidthProperty().unbind();
    overlay.prefHeightProperty().unbind();
    overlay.minWidthProperty().unbind();
    overlay.minHeightProperty().unbind();
    overlay.maxWidthProperty().unbind();
    overlay.maxHeightProperty().unbind();
  }

  private void plantOnPendingBed(
      ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType plantType) {
    if (pendingBedCol < 0 || pendingBedRow < 0) {
      return;
    }
    int tileId = getTileId(pendingBedCol, pendingBedRow);
    int[] anchor = GardenTileAtlas.bedAnchorForTile(tileId, pendingBedCol, pendingBedRow);
    if (viewModel.getPlantManager().hasPlantAt(anchor[0], anchor[1])) {
      System.out.println("Bed already has a plant");
      return;
    }
    if (!viewModel.getSeedInventory().consumeSeed(plantType)) {
      System.out.println("No seeds left for " + plantType);
      return;
    }
    if (seedPickerController != null) {
      seedPickerController.refresh();
    }
    viewModel.getPlantManager().plantSeed(plantType, anchor[0], anchor[1]);
    System.out.println("Planted " + plantType + " at bed (" + anchor[0] + ", " + anchor[1] + ")");
  }

  private void onSeedPickerClosed() {
    if (seedPickerOverlay != null) {
      unbindOverlay(seedPickerOverlay);
    }
    seedPickerController = null;
    seedPickerOverlay = null;
    pendingBedCol = -1;
    pendingBedRow = -1;
    stopUiOverlayTimerIfIdle();
  }

  private void closeSeedPicker() {
    if (seedPickerController != null) {
      seedPickerController.close();
    } else {
      onSeedPickerClosed();
    }
  }

  private void startUiOverlayTimer() {
    if (uiOverlayTimer != null) {
      return;
    }
    uiOverlayTimer =
        new AnimationTimer() {
          @Override
          public void handle(long now) {
            if (seedPickerController != null) {
              updateSeedPickerPosition();
            }
          }
        };
    uiOverlayTimer.start();
  }

  private void stopUiOverlayTimerIfIdle() {
    if (seedPickerOverlay == null && inventoryOverlay == null && uiOverlayTimer != null) {
      uiOverlayTimer.stop();
      uiOverlayTimer = null;
    }
  }

  private void updateSeedPickerPosition() {
    if (seedPickerController == null || seedPickerOverlay == null) {
      return;
    }
    if (!(canvas.getParent() instanceof StackPane stack)) {
      return;
    }

    Region panel =
        seedPickerController.getPickerBar().isVisible()
            ? seedPickerController.getPickerBar()
            : seedPickerController.getEmptyState();

    panel.applyCss();
    panel.autosize();

    double panelW = panel.getWidth();
    double panelH = panel.getHeight();
    if (panelW <= 0 || panelH <= 0) {
      panelW = panel.prefWidth(-1);
      panelH = panel.prefHeight(-1);
    }

    double viewportScale = getViewportScale();
    double playerCenterCanvasX =
        (viewModel.getPlayer().getX() + getCameraOffsetX() + 24.0) * viewportScale;
    double playerTopCanvasY = (viewModel.getPlayer().getY() + getCameraOffsetY()) * viewportScale;

    var playerOnScreen = canvas.localToScene(playerCenterCanvasX, playerTopCanvasY);
    var panelOrigin = stack.sceneToLocal(playerOnScreen);

    double x = panelOrigin.getX() - panelW / 2.0;
    double y = panelOrigin.getY() - panelH - 12.0;

    x = Math.max(8.0, Math.min(x, stack.getWidth() - panelW - 8.0));
    y = Math.max(8.0, Math.min(y, stack.getHeight() - panelH - 8.0));

    panel.setLayoutX(x);
    panel.setLayoutY(y);
  }

  private void toggleInventory() {
    if (inventoryOverlay != null) {
      closeInventory();
    } else {
      openInventory();
    }
  }

  private void openInventory() {
    closeInventory();

    Pane overlay =
        PlayerInventoryController.createPanel(
            viewModel.getPlayerInventory(),
            this::onInventoryClosed,
            controller -> {
              inventoryController = controller;
              controller.refresh();
            });

    if (overlay == null || !(canvas.getParent() instanceof StackPane stack)) {
      return;
    }

    inventoryOverlay = overlay;
    bindOverlayToStack(overlay, stack);
    stack.getChildren().add(overlay);
  }

  private void onInventoryClosed() {
    if (inventoryOverlay != null) {
      unbindOverlay(inventoryOverlay);
    }
    inventoryOverlay = null;
    inventoryController = null;
    stopUiOverlayTimerIfIdle();
  }

  private void closeInventory() {
    if (inventoryOverlay != null && canvas.getParent() instanceof StackPane stack) {
      unbindOverlay(inventoryOverlay);
      stack.getChildren().remove(inventoryOverlay);
    }
    if (inventoryController != null) {
      inventoryController.close();
    } else {
      onInventoryClosed();
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
    closeSeedPicker();
    closeInventory();
    if (uiOverlayTimer != null) {
      uiOverlayTimer.stop();
      uiOverlayTimer = null;
    }
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
    if (harvestMessageTicks > 0) {
      harvestMessageTicks--;
    }
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
    double viewportScale = getViewportScale();

    gc.save();
    gc.scale(viewportScale, viewportScale);
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

    for (var decoration : viewModel.getTileMap().getTallDecorations()) {
      renderQueue.add(
          new DepthRenderable(
              decoration.getSortY(),
              () -> decoration.render(gc, Math.round(offsetX), Math.round(offsetY))));
    }

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
                double scale = ua.notion.presentation.game.assets.PlantBasesAtlas.RENDER_SCALE;
                double destSize =
                    ua.notion.presentation.game.assets.PlantBasesAtlas.SOURCE_CELL_SIZE * scale;
                plant.render(
                    gc, screenX + 48.0 - destSize / 2.0, screenY + 48.0 - destSize / 2.0, scale);
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
    gc.restore();
  }

  private void renderUI() {
    gc.setFill(Color.WHITE);
    gc.setStroke(Color.BLACK);
    gc.setLineWidth(2);
    gc.strokeText("FPS: " + gameLoop.getFps(), 10, 20);
    gc.fillText("FPS: " + gameLoop.getFps(), 10, 20);
    gc.fillText(
        "WASD move · bed: plant/harvest · shop bench: click · I inventory · Esc menu · G grow",
        10,
        40);
    int total = viewModel.getPlayerInventory().getTotalCount();
    gc.fillText("Inventory: " + total + (total == 1 ? " item" : " items"), 10, 58);

    if (harvestMessageTicks > 0) {
      String msg = viewModel.getLastHarvestMessage();
      if (!msg.isEmpty()) {
        gc.fillText(msg, 10, 76);
      }
    }
  }

  public Canvas getCanvas() {
    return canvas;
  }

  public void setOnEscapeToMenu(Runnable onEscapeToMenu) {
    this.onEscapeToMenu = onEscapeToMenu;
  }

  public void setOnOpenShop(Runnable onOpenShop) {
    this.onOpenShop = onOpenShop;
  }
}
