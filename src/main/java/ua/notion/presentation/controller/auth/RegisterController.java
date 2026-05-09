package ua.notion.presentation.controller.auth;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ua.notion.domain.entity.User;
import ua.notion.domain.service.auth.AuthenticationService;
import ua.notion.presentation.viewmodel.RegisterViewModel;

public class RegisterController {
  private final RegisterViewModel viewModel;
  private Stage stage;
  private Runnable onRegisterSuccess;

  @FXML private TextField usernameField;
  @FXML private TextField emailField;
  @FXML private PasswordField passwordField;
  @FXML private PasswordField confirmPasswordField;
  @FXML private Label errorLabel;
  @FXML private Button registerButton;
  @FXML private Button backToLoginButton;

  public RegisterController(AuthenticationService authService) {
    this.viewModel = new RegisterViewModel(authService);
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public void setOnRegisterSuccess(Runnable callback) {
    this.onRegisterSuccess = callback;
  }

  @FXML
  public void initialize() {
    // Bind view to viewmodel
    usernameField.textProperty().bindBidirectional(viewModel.usernameProperty());
    emailField.textProperty().bindBidirectional(viewModel.emailProperty());
    passwordField.textProperty().bindBidirectional(viewModel.passwordProperty());
    confirmPasswordField.textProperty().bindBidirectional(viewModel.confirmPasswordProperty());
    errorLabel.textProperty().bind(viewModel.errorMessageProperty());
    errorLabel.visibleProperty().bind(viewModel.hasErrorProperty());

    registerButton.setOnAction(event -> handleRegister());
    backToLoginButton.setOnAction(event -> handleBackToLogin());

    passwordField.setOnAction(event -> handleRegister());
    confirmPasswordField.setOnAction(event -> handleRegister());
  }

  private void handleRegister() {
    try {
      User user = viewModel.register();
      showSuccess(
          "Account created successfully!\n\nWelcome to Garden Simulator, "
              + user.getUsername()
              + "!");
      openMenuScreen(user);
    } catch (IllegalArgumentException e) {
      // Error already set in viewModel
    } catch (Exception e) {
      // Error already set in viewModel
    }
  }

  private void openMenuScreen(User user) {
    try {
      javafx.fxml.FXMLLoader loader =
          new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
      javafx.scene.Parent root = loader.load();

      ua.notion.presentation.controller.MenuController menuController = loader.getController();
      menuController.setCurrentUser(user);

      stage.getScene().setRoot(root);
      stage.setTitle("Garden Simulator - Main Menu");
      stage.setMinWidth(800);
      stage.setMinHeight(600);
      stage.setMaxWidth(1920);
      stage.setMaxHeight(1080);
    } catch (Exception e) {
      viewModel.errorMessageProperty().set("Failed to open menu: " + e.getMessage());
      viewModel.hasErrorProperty().set(true);
    }
  }

  private void handleBackToLogin() {
    if (onRegisterSuccess != null) {
      onRegisterSuccess.run();
    }
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
