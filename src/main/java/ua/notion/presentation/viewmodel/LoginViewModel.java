package ua.notion.presentation.viewmodel;

import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import ua.notion.domain.entity.User;
import ua.notion.domain.service.auth.AuthenticationService;

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

    String user = username.get().trim();
    String pass = password.get();

    if (user.isEmpty() || pass.isEmpty()) {
      setError("Please enter username and password");
      return Optional.empty();
    }

    Optional<User> result = authService.authenticate(user, pass);

    if (result.isEmpty()) {
      setError("Invalid username or password");
    }

    return result;
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
