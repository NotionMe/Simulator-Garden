package ua.notion.presentation.controller.auth;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ua.notion.domain.entity.User;
import ua.notion.domain.service.auth.AuthenticationService;
import ua.notion.presentation.controller.MenuController;
import ua.notion.presentation.ui.SceneCoordinator;
import ua.notion.presentation.viewmodel.RegisterViewModel;

public class RegisterController {
  private final RegisterViewModel viewModel;
  private final AuthenticationService authService;

  @FXML private TextField usernameField;
  @FXML private TextField emailField;
  @FXML private PasswordField passwordField;
  @FXML private PasswordField confirmPasswordField;
  @FXML private Label errorLabel;
  @FXML private Button registerButton;
  @FXML private Button backToLoginButton;

  public RegisterController(AuthenticationService authService) {
    this.authService = authService;
    this.viewModel = new RegisterViewModel(authService);
  }

  @FXML
  public void initialize() {
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

  private SceneCoordinator coordinator() {
    return SceneCoordinator.forNode(registerButton);
  }

  private void handleRegister() {
    try {
      User user = viewModel.register();
      coordinator().hideOverlay();
      openMenuScreen(user);
    } catch (IllegalArgumentException e) {
      // validation message in viewModel
    } catch (Exception e) {
      // error in viewModel
    }
  }

  private void openMenuScreen(User user) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
      Parent root = loader.load();
      MenuController menuController = loader.getController();
      menuController.setCurrentUser(user);
      coordinator().setContent(root);
      coordinator().addStylesheet("/css/menu.css");
      coordinator().getStage().setTitle("Garden Simulator - Main Menu");
    } catch (Exception e) {
      viewModel.errorMessageProperty().set("Failed to open menu: " + e.getMessage());
      viewModel.hasErrorProperty().set(true);
    }
  }

  private void handleBackToLogin() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
      LoginController loginController = new LoginController(authService);
      loader.setController(loginController);
      Parent root = loader.load();
      coordinator().hideOverlay();
      coordinator().setContent(root);
      coordinator().addStylesheet("/css/auth.css");
      coordinator().getStage().setTitle("Garden Simulator");
    } catch (Exception e) {
      viewModel.errorMessageProperty().set("Failed to return to login: " + e.getMessage());
      viewModel.hasErrorProperty().set(true);
    }
  }
}
