package ua.notion.presentation.game;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import ua.notion.presentation.game.map.TileMap;

public class GameScene {
  private final Canvas canvas;
  private final GraphicsContext gc;
  private GameLoop gameLoop;
  private TileMap tileMap;

  public static final int TILE_SIZE = 54;
  public static final int SCALE = 1; // No scaling needed for isometric tiles
  public static final int SCALED_TILE_SIZE = TILE_SIZE * SCALE;

  public static final int MAP_WIDTH = 15; // columns
  public static final int MAP_HEIGHT = 10; // rows

  // Canvas size (larger for isometric projection)
  public static final int CANVAS_WIDTH = 960;
  public static final int CANVAS_HEIGHT = 720;

  public GameScene() {
    this.canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
    this.gc = canvas.getGraphicsContext2D();
  }

  public void initialize() {
    // Load assets and initialize game objects
    System.out.println("Initializing game scene...");

    // Create tilemap (no tileSize parameter needed anymore)
    tileMap = new TileMap(MAP_WIDTH, MAP_HEIGHT);

    // Create game loop
    gameLoop = new GameLoop(this);
  }

  public void start() {
    if (gameLoop != null) {
      gameLoop.start();
      System.out.println("Game loop started");
    }
  }

  public void stop() {
    if (gameLoop != null) {
      gameLoop.stop();
      System.out.println("Game loop stopped");
    }
  }

  public void update(double deltaTime) {
    // Update game logic here
  }

  public void render() {
    // Clear canvas
    gc.setFill(Color.BLACK);
    gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

    // Render tilemap
    if (tileMap != null) {
      tileMap.render(gc);
    }

    // Render FPS
    gc.setFill(Color.WHITE);
    gc.setStroke(Color.BLACK);
    gc.setLineWidth(2);
    gc.strokeText("FPS: " + gameLoop.getFps(), 10, 20);
    gc.fillText("FPS: " + gameLoop.getFps(), 10, 20);
  }

  public Canvas getCanvas() {
    return canvas;
  }
}
