package ua.notion.presentation.game.input;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public class InputHandler {
  private final Canvas canvas;
  private MouseClickListener mouseClickListener;

  public InputHandler(Canvas canvas) {
    this.canvas = canvas;
    setupHandlers();
  }

  private void setupHandlers() {
    canvas.setFocusTraversable(true);
    canvas.setOnMouseClicked(this::handleMouseClick);
  }

  private void handleMouseClick(MouseEvent event) {
    if (mouseClickListener != null) {
      mouseClickListener.onMouseClick(event.getX(), event.getY());
    }
  }

  public void setMouseClickListener(MouseClickListener listener) {
    this.mouseClickListener = listener;
  }

  @FunctionalInterface
  public interface MouseClickListener {
    void onMouseClick(double x, double y);
  }
}
