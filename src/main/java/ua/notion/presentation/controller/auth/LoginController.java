package ua.notion.presentation.controller.auth;

import atlantafx.base.theme.PrimerLight;
import com.google.inject.Inject;
import java.util.Optional;
import javafx.application.Application;
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
import ua.notion.presentation.viewmodel.LoginViewModel;

public class LoginController {
  @FXML private TextField usernameField;
  @FXML private PasswordField passwordField;
  @FXML private Button loginButton;
  @FXML private Button registerButton;
  @FXML private Label errorLabel;

  private final AuthenticationService authService;
  private final LoginViewModel viewModel;

  @Inject
  public LoginController(AuthenticationService authService) {
    this.authService = authService;
    this.viewModel = new LoginViewModel(authService);
  }

  @FXML
  private void initialize() {
    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

    usernameField.textProperty().bindBidirectional(viewModel.usernameProperty());
    passwordField.textProperty().bindBidirectional(viewModel.passwordProperty());
    errorLabel.textProperty().bind(viewModel.errorMessageProperty());
    errorLabel.visibleProperty().bind(viewModel.hasErrorProperty());

    loginButton.setOnAction(event -> handleLogin());
    registerButton.setOnAction(event -> handleRegister());

    usernameField.setOnAction(event -> handleLogin());
    passwordField.setOnAction(event -> handleLogin());
  }

  private SceneCoordinator coordinator() {
    return SceneCoordinator.forNode(loginButton);
  }

  private void handleLogin() {
    Optional<User> userOpt = viewModel.login();
    if (userOpt.isPresent()) {
      openMenuScreen(userOpt.get());
    }
  }

  private void openMenuScreen(User user) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
      Parent root = loader.load();
      MenuController menuController = loader.getController();
      menuController.setCurrentUser(user);
      coordinator().hideOverlay();
      coordinator().setContent(root);
      coordinator().addStylesheet("/css/menu.css");
      coordinator().getStage().setTitle("Garden Simulator - Main Menu");
    } catch (Exception e) {
      viewModel.errorMessageProperty().set("Failed to open menu: " + e.getMessage());
      viewModel.hasErrorProperty().set(true);
    }
  }

  private void handleRegister() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
      RegisterController registerController = new RegisterController(authService);
      loader.setController(registerController);
      Parent root = loader.load();
      coordinator().hideOverlay();
      coordinator().setContent(root);
      coordinator().addStylesheet("/css/auth.css");
      coordinator().getStage().setTitle("Garden Simulator - Create Account");
    } catch (Exception e) {
      viewModel.errorMessageProperty().set("Failed to open registration: " + e.getMessage());
      viewModel.hasErrorProperty().set(true);
    }
  }
}
