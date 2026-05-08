package ua.notion.presentation.viewmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import ua.notion.domain.entity.User;
import ua.notion.domain.service.auth.AuthenticationService;

public class RegisterViewModel {
  private final AuthenticationService authService;

  private final StringProperty username = new SimpleStringProperty("");
  private final StringProperty email = new SimpleStringProperty("");
  private final StringProperty password = new SimpleStringProperty("");
  private final StringProperty confirmPassword = new SimpleStringProperty("");
  private final StringProperty errorMessage = new SimpleStringProperty("");
  private final BooleanProperty hasError = new SimpleBooleanProperty(false);

  public RegisterViewModel(AuthenticationService authService) {
    this.authService = authService;
  }

  public StringProperty usernameProperty() {
    return username;
  }

  public StringProperty emailProperty() {
    return email;
  }

  public StringProperty passwordProperty() {
    return password;
  }

  public StringProperty confirmPasswordProperty() {
    return confirmPassword;
  }

  public StringProperty errorMessageProperty() {
    return errorMessage;
  }

  public BooleanProperty hasErrorProperty() {
    return hasError;
  }

  public User register() {
    clearError();

    String user = username.get().trim();
    String mail = email.get().trim();
    String pass = password.get();
    String confirmPass = confirmPassword.get();

    if (user.isEmpty() || mail.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
      setError("All fields are required");
      throw new IllegalArgumentException("All fields are required");
    }

    if (!pass.equals(confirmPass)) {
      setError("Passwords do not match");
      throw new IllegalArgumentException("Passwords do not match");
    }

    try {
      User newUser = authService.register(user, mail, pass);
      clearFields();
      return newUser;
    } catch (IllegalArgumentException e) {
      setError(e.getMessage());
      throw e;
    } catch (Exception e) {
      setError("Registration failed: " + e.getMessage());
      throw new RuntimeException("Registration failed", e);
    }
  }

  public void clearError() {
    errorMessage.set("");
    hasError.set(false);
  }

  private void setError(String message) {
    errorMessage.set(message);
    hasError.set(true);
  }

  private void clearFields() {
    username.set("");
    email.set("");
    password.set("");
    confirmPassword.set("");
  }
}
