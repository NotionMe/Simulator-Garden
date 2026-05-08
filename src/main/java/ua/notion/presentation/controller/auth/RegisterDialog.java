package ua.notion.presentation.controller.auth;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import ua.notion.domain.entity.User;
import ua.notion.domain.service.auth.AuthenticationService;

public class RegisterDialog extends Dialog<User> {
  private final AuthenticationService authService;
  private final TextField usernameField;
  private final TextField emailField;
  private final PasswordField passwordField;
  private final PasswordField confirmPasswordField;

  public RegisterDialog(AuthenticationService authService) {
    this.authService = authService;

    setTitle("Create Account");
    setHeaderText("Join Garden Simulator");

    ButtonType registerButtonType = new ButtonType("Create Account", ButtonBar.ButtonData.OK_DONE);
    getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(15);
    grid.setVgap(15);
    grid.setPadding(new Insets(20, 20, 10, 20));

    usernameField = new TextField();
    usernameField.setPromptText("Choose a username");
    usernameField.setPrefWidth(280);
    usernameField.getStyleClass().add("custom-text-field");

    emailField = new TextField();
    emailField.setPromptText("your.email@example.com");
    emailField.setPrefWidth(280);
    emailField.getStyleClass().add("custom-text-field");

    passwordField = new PasswordField();
    passwordField.setPromptText("Create a password");
    passwordField.setPrefWidth(280);
    passwordField.getStyleClass().add("custom-text-field");

    confirmPasswordField = new PasswordField();
    confirmPasswordField.setPromptText("Confirm your password");
    confirmPasswordField.setPrefWidth(280);
    confirmPasswordField.getStyleClass().add("custom-text-field");

    Label usernameLabel = new Label("Username");
    usernameLabel.getStyleClass().add("field-label");
    Label emailLabel = new Label("Email");
    emailLabel.getStyleClass().add("field-label");
    Label passwordLabel = new Label("Password");
    passwordLabel.getStyleClass().add("field-label");
    Label confirmLabel = new Label("Confirm Password");
    confirmLabel.getStyleClass().add("field-label");

    grid.add(usernameLabel, 0, 0);
    grid.add(usernameField, 0, 1);
    grid.add(emailLabel, 0, 2);
    grid.add(emailField, 0, 3);
    grid.add(passwordLabel, 0, 4);
    grid.add(passwordField, 0, 5);
    grid.add(confirmLabel, 0, 6);
    grid.add(confirmPasswordField, 0, 7);

    VBox requirementsBox = new VBox(5);
    requirementsBox.setPadding(new Insets(10, 0, 0, 0));
    Label requirementsTitle = new Label("Requirements:");
    requirementsTitle.setStyle(
        "-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #558b2f;");
    Label req1 = new Label("• Username: 3-50 characters, alphanumeric + underscore");
    req1.setStyle("-fx-font-size: 10px; -fx-text-fill: #7cb342;");
    Label req2 = new Label("• Password: minimum 8 characters");
    req2.setStyle("-fx-font-size: 10px; -fx-text-fill: #7cb342;");
    Label req3 = new Label("• Valid email format");
    req3.setStyle("-fx-font-size: 10px; -fx-text-fill: #7cb342;");
    requirementsBox.getChildren().addAll(requirementsTitle, req1, req2, req3);
    grid.add(requirementsBox, 0, 8);

    getDialogPane().setContent(grid);
    getDialogPane().getStyleClass().add("dialog-pane");

    Button registerButton = (Button) getDialogPane().lookupButton(registerButtonType);
    registerButton.addEventFilter(
        javafx.event.ActionEvent.ACTION,
        event -> {
          if (!validateAndRegister()) {
            event.consume();
          }
        });

    setResultConverter(
        dialogButton -> {
          if (dialogButton == registerButtonType) {
            return null;
          }
          return null;
        });
  }

  private boolean validateAndRegister() {
    String username = usernameField.getText().trim();
    String email = emailField.getText().trim();
    String password = passwordField.getText();
    String confirmPassword = confirmPasswordField.getText();

    if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
      showError("All fields are required");
      return false;
    }

    if (!password.equals(confirmPassword)) {
      showError("Passwords do not match");
      return false;
    }

    try {
      User user = authService.register(username, email, password);
      showSuccess("Account created successfully!\n\nYou can now log in with your credentials.");
      setResult(user);
      return true;
    } catch (IllegalArgumentException e) {
      showError(e.getMessage());
      return false;
    } catch (Exception e) {
      showError("Registration failed: " + e.getMessage());
      return false;
    }
  }

  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Registration Error");
    alert.setHeaderText("Unable to create account");
    alert.setContentText(message);
    alert.getDialogPane().getStyleClass().add("dialog-pane");
    alert.showAndWait();
  }

  private void showSuccess(String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Success");
    alert.setHeaderText("Welcome to Garden Simulator!");
    alert.setContentText(message);
    alert.getDialogPane().getStyleClass().add("dialog-pane");
    alert.showAndWait();
  }
}
