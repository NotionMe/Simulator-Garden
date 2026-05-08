package ua.notion.presentation.game.input;

import java.util.HashSet;
import java.util.Set;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class KeyboardHandler {
  private final Set<KeyCode> pressedKeys = new HashSet<>();
  private final Set<KeyCode> justPressedKeys = new HashSet<>();

  public void handleKeyPressed(KeyEvent event) {
    KeyCode code = event.getCode();
    if (!pressedKeys.contains(code)) {
      justPressedKeys.add(code);
    }
    pressedKeys.add(code);
  }

  public void handleKeyReleased(KeyEvent event) {
    pressedKeys.remove(event.getCode());
  }

  public boolean isPressed(KeyCode key) {
    return pressedKeys.contains(key);
  }

  public boolean isJustPressed(KeyCode key) {
    return justPressedKeys.contains(key);
  }

  public void clearJustPressed() {
    justPressedKeys.clear();
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
