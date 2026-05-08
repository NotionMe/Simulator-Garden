package ua.notion.presentation.game.input;

import java.util.HashSet;
import java.util.Set;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class KeyboardHandler {
  private final Set<KeyCode> pressedKeys = new HashSet<>();

  public void handleKeyPressed(KeyEvent event) {
    pressedKeys.add(event.getCode());
  }

  public void handleKeyReleased(KeyEvent event) {
    pressedKeys.remove(event.getCode());
  }

  public boolean isPressed(KeyCode key) {
    return pressedKeys.contains(key);
  }

  public boolean isMovingUp() {
    return isPressed(KeyCode.W) || isPressed(KeyCode.UP);
  }

  public boolean isMovingDown() {
    return isPressed(KeyCode.S) || isPressed(KeyCode.DOWN);
  }

  public boolean isMovingLeft() {
    return isPressed(KeyCode.A) || isPressed(KeyCode.LEFT);
  }

  public boolean isMovingRight() {
    return isPressed(KeyCode.D) || isPressed(KeyCode.RIGHT);
  }
}
