package ua.notion.presentation.viewmodel;

import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import ua.notion.domain.entity.User;
import ua.notion.domain.service.auth.AuthenticationService;
import ua.notion.infrastructure.websocket.WebSocketApiException;

public class LoginViewModel {
  private final AuthenticationService authService;

  private final StringProperty username = new SimpleStringProperty("");
  private final StringProperty password = new SimpleStringProperty("");
  private final StringProperty errorMessage = new SimpleStringProperty("");
  private final BooleanProperty hasError = new SimpleBooleanProperty(false);

  public LoginViewModel(AuthenticationService authService) {
    this.authService = authService;
  }

  public StringProperty usernameProperty() {
    return username;
  }

  public StringProperty passwordProperty() {
    return password;
  }

  public StringProperty errorMessageProperty() {
    return errorMessage;
  }

  public BooleanProperty hasErrorProperty() {
    return hasError;
  }

  public Optional<User> login() {
    clearError();

    String identifier = username.get().trim();
    String pass = password.get();

    if (identifier.isEmpty() || pass.isEmpty()) {
      setError("Please enter username or email and password");
      return Optional.empty();
    }

    try {
      Optional<User> result = authService.login(identifier, pass);
      if (result.isEmpty()) {
        setError("Invalid username or password");
      }
      return result;
    } catch (IllegalArgumentException e) {
      setError(e.getMessage());
      return Optional.empty();
    } catch (WebSocketApiException e) {
      setError(mapServerLoginError(e.getMessage()));
      return Optional.empty();
    }
  }

  private static String mapServerLoginError(String serverMessage) {
    if (serverMessage == null || serverMessage.isBlank()) {
      return "Login failed";
    }
    return switch (serverMessage) {
      case "Invalid credentials" -> "Invalid username or password";
      case "Record not found" -> "User not found";
      default -> serverMessage;
    };
  }

  public void clearError() {
    errorMessage.set("");
    hasError.set(false);
  }

  private void setError(String message) {
    errorMessage.set(message);
    hasError.set(true);
  }
}
